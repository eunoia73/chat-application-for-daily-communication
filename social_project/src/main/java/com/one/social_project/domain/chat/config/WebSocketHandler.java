package com.one.social_project.domain.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.social_project.domain.chat.dto.ChatMessageDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.ChatRoomRepository;
import com.one.social_project.domain.chat.service.ChatMessageService;
import com.one.social_project.domain.chat.service.ReadReceiptService;
import com.one.social_project.domain.user.util.TokenProvider;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>(); // 채팅방 별로 WebSocket 세션을 관리하기 위한 Map
    private final ChatMessageService chatMessageService;
    private final ReadReceiptService readReceiptService;
    private final ObjectMapper objectMapper;
    private final TokenProvider tokenProvider;

    /**
     * WebSocket 연결 시 호출
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String roomId = extractRoomId(session);
            String nickname = extractNickname(session);
            roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);

            // 입장 메시지 처리
            ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                    .roomId(roomId)
                    .sender(nickname)
                    .messageType("ENTER")
                    .build();

            // 입장 메시지를 브로드캐스트 및 처리
            handleEnterMessage(session, chatMessageDTO);

            log.info("WebSocket 연결 성공: roomId={}, sender={}", roomId, nickname);
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

            switch (chatMessageDTO.getMessageType()) {
                case "CHAT":
                    handleChatMessage(session, chatMessageDTO);
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

        // 읽지 않은 메시지 처리
        handleReadAllUnreadMessages(roomId, sender);

        // 입장 메시지 브로드캐스트
        Map<String, Object> joinMessage = Map.of(
                "chatType", "ENTER",
                "message", sender + "님이 입장하였습니다."
        );
        broadcast(roomId, createTextMessage(joinMessage)); // 중복 호출 확인
        log.info("ENTER 메시지 처리 완료: roomId={}, sender={}", roomId, sender);
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
    private void handleChatMessage(WebSocketSession session, ChatMessageDTO chatMessageDTO) {
        String roomId = chatMessageDTO.getRoomId();
        String nickname = extractNickname(session);

        LocalDateTime createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        chatMessageDTO.setSender(nickname); // 닉네임으로 업데이트
        chatMessageDTO.setCreatedAt(createdAt);

        String messageId = chatMessageService.saveMessage(roomId, nickname, chatMessageDTO.getMessage());
        readReceiptService.markAsRead(messageId, nickname);

        List<String> readers = readReceiptService.getReadBy(messageId);

        Map<String, Object> readReceiptResponse = Map.of(
                "roomId", roomId,
                "messageId", messageId,
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
            String nickname = extractNickname(session);

        try {
            int unreadCount = readReceiptService.countUnreadMessages(roomId, nickname);

            Map<String, Object> response = Map.of(
                    "chatType", "UNREAD_COUNT",
                    "roomId", roomId,
                    "nickname", nickname,
                    "unreadCount", unreadCount
            );

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

    private String extractToken(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) {
                throw new IllegalArgumentException("WebSocket Session URI가 존재하지 않습니다.");
            }

            String query = uri.getQuery();
            if (query == null || query.isEmpty()) {
                throw new IllegalArgumentException("WebSocket URI에 쿼리 문자열이 없습니다.");
            }

            // 쿼리 파라미터를 파싱하여 토큰 추출
            Map<String, String> queryParams = Arrays.stream(query.split("&"))
                    .map(param -> param.split("=", 2))
                    .collect(Collectors.toMap(
                            parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                            parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    ));

            // "token" 파라미터를 추출
            String token = queryParams.get("token");
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("WebSocket URI에 'token' 파라미터가 존재하지 않습니다.");
            }

            return token; // 토큰 반환
        } catch (Exception e) {
            log.error("Token 추출 실패: {}", e.getMessage(), e);
            throw new IllegalArgumentException("WebSocket 요청에서 Token 정보를 추출할 수 없습니다.", e);
        }
    }

    private String extractNickname(WebSocketSession session) {
        try {
            String token = extractToken(session); // Token 추출
            return tokenProvider.getNicknameFromToken(token); // Token에서 닉네임 추출
        } catch (Exception e) {
            log.error("Nickname 정보 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("WebSocket 요청에서 닉네임 정보를 추출할 수 없습니다.", e);
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
