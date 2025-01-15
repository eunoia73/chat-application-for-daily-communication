package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ChatParticipantsDTO;
import com.one.social_project.domain.chat.constant.ChatRole;
import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.constant.ChatRoomType;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.entity.ChatParticipants;
import com.one.social_project.domain.chat.entity.ChatRoom;
import com.one.social_project.domain.chat.repository.ChatMessageRepository;
import com.one.social_project.domain.chat.repository.ChatParticipantsRepository;
import com.one.social_project.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;

    // 채팅방 생성 (개인 채팅방 또는 그룹 채팅방)
    public ChatRoomDTO createChatRoom(List<String> participants) {
        if (participants.size() == 2) {
            // 개인 채팅방 생성
            return createDirectChatRoom(participants.get(0), participants.get(1));
        } else if (participants.size() > 2) {
            // 그룹 채팅방 생성
            return createGroupChatRoom(participants);
        } else {
            throw new IllegalArgumentException("참여자가 2명 이상이어야 합니다.");
        }
    }

    // 특정 채팅방 조회
    public ChatRoomDTO getChatRoom(String roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        return convertToDTO(chatRoom);
    }

    // 모든 채팅방 조회
    public List<ChatRoomDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 특정 채팅방 참여자 상세 조회
    public List<ChatParticipantsDTO> getChatRoomParticipants(String roomId){
        return chatParticipantsRepository.findByChatRoomRoomId(roomId)
                .stream()
                .map(chatParticipants -> convertToParticipantsDTO(chatParticipants, roomId))
                .collect(Collectors.toList());
    }

    // 채팅방 나가기 및 채팅방 참여자 없을 시 채팅방 삭제
    public String leaveChatRoom(String roomId, String userId) {
        // 채팅방 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 참여자 확인
        ChatParticipants participant = chatParticipantsRepository.findByChatRoomRoomIdAndUserId(roomId, userId)
                        .orElseThrow(() -> new RuntimeException("참여자가 채팅방에 존재하지 않습니다."));

        //  채팅방 퇴장 시, OWNER 역할을 다른 참여자에게 위임
        if(participant.getChatRole() == ChatRole.OWNER){
            // OWNER 역할을 다른 참여자에게 위임
            List<ChatParticipants> checkParticipants = chatParticipantsRepository.findByChatRoomRoomId(roomId);
            for(ChatParticipants checkParticipant : checkParticipants){
                if(checkParticipant.getChatRole() == ChatRole.MEMBER){
                    // 첫 번째 MEMBER 에게 OWNER 역할 부여
                    checkParticipant.setChatRole(ChatRole.OWNER);
                    chatParticipantsRepository.save(checkParticipant);
                    break;
                }
            }
        }

        // 참여자 제거
        chatParticipantsRepository.delete(participant);

        // 채팅방에 남은 참여자가 없으면 채팅방 사용
        List<ChatParticipants> checkParticipants = chatParticipantsRepository.findByChatRoomRoomId(roomId);
        if(checkParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom);
            return "채팅방이 삭제되었습니다.";
        }
        return "채팅방에서 나갔습니다.";
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // 개인 채팅방 생성
    private ChatRoomDTO createDirectChatRoom(String user1, String user2) {
        String roomId = generatePersonalRoomId(user1, user2);

        // 기존 채팅방 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomId(roomId);
        if (existingRoom.isPresent()) {
            return convertToDTO(existingRoom.get());
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(user1 + " & " + user2)
                .roomType(ChatRoomType.DM)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);

        // 참여자 추가
        chatParticipantsRepository.save(
                ChatParticipants.builder()
                        .chatRoom(chatRoom)
                        .userId(user1)
                        .chatRole(ChatRole.OWNER) // 방장
                        .build());

        chatParticipantsRepository.save(
                ChatParticipants.builder()
                        .chatRoom(chatRoom)
                        .userId(user2)
                        .chatRole(ChatRole.MEMBER) // 일반 멤버
                        .build());

        return convertToDTO(chatRoom);
    }

    // 그룹 채팅방 생성
    private ChatRoomDTO createGroupChatRoom(List<String> participants) {
        if(participants.isEmpty()){
            throw new IllegalArgumentException("참여자는 최소 1명 이상이어야 합니다.");
        }

        String roomId = UUID.randomUUID().toString();

        // 사용자 이름을 정렬 후 이름 조합 생성
        String roomName = participants.stream()
                .sorted()
                .collect(Collectors.joining(", "));

        // 새 그룹 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomType(ChatRoomType.GM)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);

        // 참여자 추가
        for(int i = 0 ; i < participants.size(); i++){
            ChatRole chatRole = (i == 0) ? ChatRole.OWNER : ChatRole.MEMBER;
            chatParticipantsRepository.save(
                    ChatParticipants.builder()
                            .chatRoom(chatRoom)
                            .userId(participants.get(i))
                            .chatRole(chatRole)
                            .build());
        }

        return convertToDTO(chatRoom);
    }

    // 두 사용자 ID를 정렬하여 고유한 roomId 생성
    private String generatePersonalRoomId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + ":" + user2 : user2 + ":" + user1;
    }

    // ChatRoom Entity -> DTO
    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        // 참여자 정보를 추출 및 DTO 변환
        List<String> participantUserIds = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .map(ChatParticipants::getUserId)
                .collect(Collectors.toList());

        // 방장 정보 추출 (OWNER 역할을 가진 참여자)
        String ownerId = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .filter(participant -> participant.getChatRole() == ChatRole.OWNER)
                .map(ChatParticipants::getUserId)
                .findFirst()
                .orElse("Unknown");  // OWNER가 없으면 "Unknown"을 기본값으로 설정

        // 최근 메시지 조회
        ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoomRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .ownerId(ownerId)
                .roomType(chatRoom.getRoomType())
                .createdAt(chatRoom.getCreatedAt())
                .participants(participantUserIds)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .build();
    }

    // ChatParticipants Entity -> DTO
    private ChatParticipantsDTO convertToParticipantsDTO(ChatParticipants participants, String roomId){
        return ChatParticipantsDTO.builder()
                .roomId(roomId)
                .userId(participants.getUserId())
                .role(participants.getChatRole())
                .build();
    }
}
