package com.one.social_project.domain.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.social_project.domain.chat.dto.ChatMessageDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.service.ChatMessageService;
import com.one.social_project.domain.chat.service.ReadReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>(); // 채팅방 별로 WebSocket 세션을 관리하기 위한 Map
    private final ChatMessageService chatMessageService;
    private final ReadReceiptService readReceiptService;
    private final ObjectMapper objectMapper;

    /**
     * WebSocket 연결 시 호출
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String roomId = extractRoomId(session);
            roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
            log.info("WebSocket 연결 성공: roomId={}", roomId);
        } catch (Exception e) {
            log.error("WebSocket 연결 실패: {}", e.getMessage(), e);
            closeSession(session, CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 클라이언트 메시지 처리
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            ChatMessageDTO chatMessageDTO = objectMapper.readValue(payload, ChatMessageDTO.class);

            switch (chatMessageDTO.getChatType()) {
                case "ENTER":
                    handleEnterMessage(session, chatMessageDTO);
                    break;
                case "CHAT":
                    handleChatMessage(chatMessageDTO);
                    break;
                case "UNREAD_COUNT":
                    handleUnreadCountRequest(chatMessageDTO, session);
                    break;
                default:
                    sendError(session, "알 수 없는 chatType입니다.");
            }
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            sendError(session, "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자가 채팅방에 입장 시 처리
     */
    private void handleEnterMessage(WebSocketSession session, ChatMessageDTO chatMessageDTO) {
        String roomId = chatMessageDTO.getRoomId();
        String sender = chatMessageDTO.getSender();

        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("ENTER 메시지 처리 완료: roomId={}, sender={}", roomId, sender);

        // 읽지 않은 메시지 처리
        handleReadAllUnreadMessages(roomId, sender);
    }

    /**
     * 읽지 않은 메시지를 모두 읽음 처리
     */
    private void handleReadAllUnreadMessages(String roomId, String sender) {
        List<ChatMessage> unreadMessages = chatMessageService.getUnreadMessages(roomId, sender);

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(chatMessage -> {
                chatMessageService.markMessageAsRead(chatMessage.getId(), sender);
                readReceiptService.markAsRead(chatMessage.getId(), sender);
            });

            int unreadCount = readReceiptService.countUnreadMessages(roomId, sender);

            Map<String, Object> response = Map.of(
                    "chatType", "UNREAD_COUNT",
                    "roomId", roomId,
                    "unreadCount", unreadCount
            );

            broadcast(roomId, createTextMessage(response));
            log.info("읽지 않은 메시지 처리 완료: roomId={}, sender={}, 처리된 메시지 수={}",
                    roomId, sender, unreadMessages.size());
        } else {
            log.info("읽지 않은 메시지가 없습니다: roomId={}, sender={}", roomId, sender);
        }
    }

    /**
     * 채팅 메시지 처리
     */
    private void handleChatMessage(ChatMessageDTO chatMessageDTO) {
        String roomId = chatMessageDTO.getRoomId();
        String sender = chatMessageDTO.getSender();
        String message = chatMessageDTO.getMessage();

        if (!roomSessions.containsKey(roomId)) {
            log.error("Error: 채팅방이 존재하지 않습니다. roomId={}", roomId);
            return;
        }

        String messageId = chatMessageService.saveMessage(roomId, sender, message);

        readReceiptService.markAsRead(messageId, sender);

        List<String> readers = readReceiptService.getReadBy(messageId);

        Map<String, Object> readReceiptResponse = Map.of(
                "roomId", roomId,
                "message", messageId,
                "readBy", readers
        );

        broadcast(roomId, createTextMessage(readReceiptResponse));
        broadcast(roomId, createTextMessage(chatMessageDTO));
    }

    /**
     * 사용자의 채팅방 읽지 않은 메시지 수 요청 처리
     */
    private void handleUnreadCountRequest(ChatMessageDTO chatMessageDTO, WebSocketSession session) {
        String roomId = chatMessageDTO.getRoomId();
        String userId = chatMessageDTO.getSender();

        int unreadCount = readReceiptService.countUnreadMessages(roomId, userId);

        Map<String, Object> response = Map.of(
                "chatType", "UNREAD_COUNT",
                "roomId", roomId,
                "unreadCount", unreadCount
        );

        try {
            session.sendMessage(createTextMessage(response));
        } catch (IOException e) {
            log.error("Unread count 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 메시지를 모든 WebSocket 세션에 브로드캐스트
     */
    private void broadcast(String roomId, TextMessage message) {
        roomSessions.getOrDefault(roomId, List.of()).forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("메시지 전송 실패: roomId={}, error={}", roomId, e.getMessage(), e);
                }
            }
        });
    }

    /**
     * WebSocket 연결 종료 처리
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            String roomId = extractRoomId(session);
            List<WebSocketSession> sessions = roomSessions.getOrDefault(roomId, new CopyOnWriteArrayList<>());
            sessions.remove(session);

            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
            log.info("WebSocket 연결 종료: roomId={}", roomId);
        } catch (Exception e) {
            log.error("WebSocket 종료 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * WebSocket URI에서 채팅방 ID 추출
     */
    private String extractRoomId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null || uri.getPath() == null) {
                throw new IllegalArgumentException("Invalid URI");
            }
            String[] segments = uri.getPath().split("/");
            return URLDecoder.decode(segments[segments.length - 1], StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract roomId", e);
        }
    }

    /**
     * JSON 데이터를 TextMessage로 변환
     */
    private TextMessage createTextMessage(Object data) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            throw new RuntimeException("메시지 직렬화 실패", e);
        }
    }

    /**
     * WebSocket 세션 종료 처리
     */
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            log.error("세션 종료 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 오류 메시지 전송
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            session.sendMessage(new TextMessage(errorMessage));
        } catch (IOException e) {
            log.error("오류 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }
}
