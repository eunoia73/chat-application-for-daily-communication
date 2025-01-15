package com.one.social_project.domain.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.social_project.domain.chat.dto.ChatMessageDTO;
import com.one.social_project.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//Spring WebSocket을 사용하여 채팅 기능을 제공하기 위한 WebSocket 핸들러 클래스입니다.
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    // 채팅방 별로 WebSocket 세션을 관리하기 위한 Map
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService; // 채팅 메시지 저장 및 관리 서비스
    private final ObjectMapper objectMapper; // JSON 파싱 도구

    /**
     * WebSocket 연결이 성공적으로 생성되었을 때 호출됩니다.
     * 해당 세션을 채팅방(roomId)에 추가합니다.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String roomId = getRoomIdFromSession(session); // 세션에서 채팅방 ID 추출
        // 해당 채팅방 세션 리스트 생성(없으면 새로 생성)
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        // 중복되지 않은 세션만 추가
        if (!sessions.contains(session)) {
            sessions.add(session);
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

            // 메시지를 데이터베이스에 저장
            chatMessageService.saveMessage(
                    chatMessageDTO.getRoomId(),
                    chatMessageDTO.getSender(),
                    chatMessageDTO.getMessage()
            );

            String roomId = chatMessageDTO.getRoomId(); // 메시지의 채팅방 ID
            // 해당 채팅방의 모든 세션에 메시지 전송
            for (WebSocketSession s : roomSessions.getOrDefault(roomId, new ArrayList<>())) {
                if (s.isOpen()) { // 세션이 열려 있는 경우만 전송
                    s.sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 스택 트레이스 출력
        }
    }

    // WebSocket 연결이 종료되었을 때 호출됩니다.
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getRoomIdFromSession(session); // 세션에서 채팅방 ID 추출
        // 해당 채팅방에서 세션 제거
        roomSessions.getOrDefault(roomId, new ArrayList<>()).remove(session);
    }

    // WebSocket 세션의 URI에서 채팅방 ID를 추출합니다.
    private String getRoomIdFromSession(WebSocketSession session) {
        return session.getUri().getQuery().split("=")[1]; // URI 쿼리 파라미터에서 roomId 값 추출
    }
}
