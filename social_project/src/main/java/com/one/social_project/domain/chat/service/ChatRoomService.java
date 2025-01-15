package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ChatParticipantsDTO;
import com.one.social_project.domain.chat.dto.ChatRole;
import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.dto.ChatRoomType;
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

        // 참여자 ID 목록 추출
        List<String> participantsUserIds = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .map(ChatParticipants::getUserId)
                .collect(Collectors.toList());

        // 최근 메시지 조회
        ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoomRoomIdOrderByCreatedAtDesc(roomId);

        // DTO 반환
        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .roomType(chatRoom.getRoomType())
                .ownerId(chatRoom.getOwnerId())
                .createdAt(chatRoom.getCreatedAt())
                .participants(participantsUserIds)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .build();
    }

    // 모든 채팅방 조회
    public List<ChatRoomDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();

        // 최근 메시지 조회
        return chatRooms.stream().map(chatRoom -> {
            ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoomRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());

            // 참여자 ID 목록 추출
            List<String> participantsUserIds = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                    .stream()
                    .map(ChatParticipants::getUserId)
                    .collect(Collectors.toList());

            // DTO 반환
            return ChatRoomDTO.builder()
                    .roomId(chatRoom.getRoomId())
                    .roomName(chatRoom.getRoomName())
                    .roomType(chatRoom.getRoomType())
                    .ownerId(chatRoom.getOwnerId())
                    .createdAt(chatRoom.getCreatedAt())
                    .participants(participantsUserIds)
                    .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    // 특정 채팅방 참여자 상세 조회
    public List<ChatParticipantsDTO> getChatRoomParticipants(String roomId){
        return chatParticipantsRepository.findByChatRoomRoomId(roomId)
                .stream()
                .map(chatParticipants -> ChatParticipantsDTO.builder()
                        .roomId(roomId)
                        .userId(chatParticipants.getUserId())
                        .role(chatParticipants.getChatRole())
                        .build())
                .collect(Collectors.toList());
    }

    // 채팅방 삭제(추후 삭제 -> 나가기)
    public String deleteChatRoom(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        chatRoomRepository.delete(chatRoom);
        return "채팅방 삭제 완료!";
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
                .ownerId(user1) // 첫 번째 사용자를 방장으로 결정
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);

        // 참여자 추가
        chatParticipantsRepository.save(
                ChatParticipants.builder()
                        .chatRoom(chatRoom)
                        .userId(user1)
                        .chatRole(ChatRole.OWNER) // 방장
                        .build()
        );
        chatParticipantsRepository.save(
                ChatParticipants.builder()
                        .chatRoom(chatRoom)
                        .userId(user2)
                        .chatRole(ChatRole.MEMBER) // 일반 멤버
                        .build()
        );

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
                .ownerId(participants.get(0)) // 첫 번째 참가자에게 방장 권한 부여
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
                            .build()
            );
        }
        return convertToDTO(chatRoom);
    }

    // 두 사용자 ID를 정렬하여 고유한 roomId 생성
    private String generatePersonalRoomId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + ":" + user2 : user2 + ":" + user1;
    }

    // entity -> dto
    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        // 참여자 정보를 추출 및 DTO 변환
        List<ChatParticipantsDTO> participantsDTOS = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .map(chatParticipants -> ChatParticipantsDTO.builder()
                        .roomId(chatRoom.getRoomId())
                        .userId(chatParticipants.getUserId())
                        .role(chatParticipants.getChatRole())
                        .build())
                .toList();

        // 참여자 ID 목록 생성
        List<String> participantUserIds = participantsDTOS.stream()
                .map(ChatParticipantsDTO::getUserId)
                .collect(Collectors.toList());


        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .ownerId(chatRoom.getOwnerId())
                .roomType(chatRoom.getRoomType())
                .createdAt(chatRoom.getCreatedAt())
                .participants(participantUserIds)
                .build();
    }
}
