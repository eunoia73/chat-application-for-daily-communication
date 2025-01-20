package com.one.social_project.domain.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.social_project.domain.chat.dto.ChatMessageDTO;
import com.one.social_project.domain.chat.dto.ReadReceiptDTO;
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

//Spring WebSocket을 사용하여 채팅 기능을 제공하기 위한 WebSocket 핸들러 클래스입니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    // 채팅방 별로 WebSocket 세션을 관리하기 위한 Map
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService; // 채팅 메시지 저장 및 관리 서비스
    private final ReadReceiptService readReceiptService;
    private final ObjectMapper objectMapper; // JSON 파싱 도구

    /**
     * WebSocket 연결이 성공적으로 생성되었을 때 호출됩니다.
     * 해당 세션을 채팅방(roomId)에 추가합니다.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String roomId = extractRoomId(session); // 세션에서 채팅방 ID 추출

            // 해당 채팅방 세션 리스트 생성(없으면 새로 생성)
            roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);
            log.info("WebSocket 연결 성공: roomId={}", roomId);

        } catch (Exception e) {
            log.error("WebSocket 연결 실패: {}", e.getMessage(), e);
            closeSession(session, CloseStatus.SERVER_ERROR); // 세션 종료 확인
        }
    }

    /**
     * 클라이언트가 메시지를 보낼 때 호출됩니다.
     * 메시지를 JSON으로 파싱하고 해당 채팅방의 모든 세션에 메시지를 브로드캐스트합니다.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload(); // 클라이언트에서 전송된 메시지 내용
            System.out.println("Received payload: " + payload);

            // JSON 문자열을 ChatMessageDTO 객체로 변환
            ChatMessageDTO chatMessageDTO = objectMapper.readValue(payload, ChatMessageDTO.class);
            validateMessage(chatMessageDTO); // 메시지 유효성 검사

            switch (chatMessageDTO.getChatType()) {
                case "CHAT":
                    handleChatMessage(chatMessageDTO); // 채팅 메시지 처리
                    break;
                case "READ_RECEIPT":
                    ReadReceiptDTO readReceiptDTO = objectMapper.readValue(message.getPayload(), ReadReceiptDTO.class);
                    handleReadReceipt(readReceiptDTO); // 읽음 상태 처리
                    break;
                default:
                    sendMessage(session, "Error: 알 수 없는 chatType입니다.");
            }
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", e.getMessage(), e);
            sendMessage(session, "Error: 메시지 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 채팅 메시지 처리.
     */
    private void handleChatMessage(ChatMessageDTO chatMessageDTO) {
        String roomId = chatMessageDTO.getRoomId();
        String sender = chatMessageDTO.getSender();
        String message = chatMessageDTO.getMessage();

        if (!roomSessions.containsKey(roomId)) {
            log.error("Error: 채팅방이 존재하지 않습니다. roomId={}", roomId);
            return;
        }

        // 메시지 저장 및 보낸 사람을 자동으로 readers에 추가
        String messageId = chatMessageService.saveMessage(roomId, sender, message);

        // 읽음 상태를 즉시 브로드캐스트
        List<String> readers = readReceiptService.getReadBy(messageId);

        Map<String, Object> readReceiptResponse = Map.of(
                "roomId", roomId,
                "message", messageId,
                "readBy", readers // 서버에서 관리하는 읽은 사람 목록
        );

        broadcast(roomId, createTextMessage(readReceiptResponse)); // 읽음 상태 브로드캐스트
        broadcast(roomId, createTextMessage(chatMessageDTO)); // 채팅 메시지 브로드캐스트
    }

    /**
     * 읽음 상태 처리.
     */
    private void handleReadReceipt(ReadReceiptDTO readReceiptDTO) {
        String roomId = readReceiptDTO.getRoomId();
        String messageId = readReceiptDTO.getMessage();
        String sender = readReceiptDTO.getSender(); // 읽음을 보고한 사용자

        // 필드 유효성 검사
        if (roomId == null || messageId == null || sender == null) {
            log.error("Invalid READ_RECEIPT data: {}", readReceiptDTO);
            return;
        }

        // 메시지 읽음 처리 (sender를 읽은 사용자로 추가)
        readReceiptService.markAsRead(messageId, sender);

        // 읽음 상태 브로드캐스트 (readBy는 서버에서 조회)
        List<String> readers = readReceiptService.getReadBy(messageId); // DB에서 읽은 사람 목록 조회

        Map<String, Object> readReceiptResponse = Map.of(
                "roomId", roomId,
                "messageId", messageId,
                "readBy", readers // 서버가 관리하는 읽은 사람 목록
        );

        broadcast(roomId, createTextMessage(readReceiptResponse));
    }

    /**
     * 특정 채팅방의 모든 WebSocket 세션에 메시지를 브로드캐스트.
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
     * WebSocket 연결이 종료되었을 때 호출됩니다.
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
     * WebSocket 세션의 URI에서 채팅방 ID를 추출합니다.
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
     * 메시지 유효성 검사.
     */
    private void validateMessage(ChatMessageDTO chatMessageDTO) {
        if (chatMessageDTO.getChatType() == null || chatMessageDTO.getRoomId() == null) {
            throw new IllegalArgumentException("Error: chatType 또는 roomId가 누락되었습니다.");
        }
    }

    /**
     * TextMessage 객체를 생성.
     */
    private TextMessage createTextMessage(Object data) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            throw new RuntimeException("메시지 직렬화 실패", e);
        }
    }

    /**
     * WebSocket 세션 종료 처리.
     */
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            log.error("세션 종료 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 오류 메시지 전송.
     */
    private void sendMessage(WebSocketSession session, String errorMessage) {
        try {
            session.sendMessage(new TextMessage(errorMessage));
        } catch (IOException e) {
            log.error("오류 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }
}
