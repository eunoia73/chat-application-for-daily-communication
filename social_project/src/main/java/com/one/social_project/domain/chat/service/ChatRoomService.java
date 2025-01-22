package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ChatParticipantsDTO;
import com.one.social_project.domain.chat.constant.ChatRole;
import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.constant.ChatRoomType;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.entity.ChatParticipants;
import com.one.social_project.domain.chat.entity.ChatRoom;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import com.one.social_project.domain.chat.repository.ChatParticipantsRepository;
import com.one.social_project.domain.chat.repository.ChatRoomRepository;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;

    // 채팅방 생성 (개인 채팅방 또는 그룹 채팅방)
    public ChatRoomDTO createChatRoom(String roomName, List<String> participants) {

        List<User> users = participants.stream()
                .map(nickname ->{
                    User user = userRepository.findByNickname(nickname)
                            .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + nickname));
                    return user;
                }).collect(Collectors.toList());
        if (users.size() == 2) {
            // 개인 채팅방 생성
            return createDirectChatRoom(users.get(0), users.get(1), roomName);
        } else if (users.size() > 2) {
            // 그룹 채팅방 생성
            return createGroupChatRoom(roomName, users);
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

    // 사용자별 채팅방 조회
    public List<ChatRoomDTO> getUserChatRooms(String nickName){
        // 사용자가 참여 중인 채팅방 ID 목록 조회
        List<String> roomId = chatParticipantsRepository.findByUserNickname(nickName)
                .stream()
                .map(participants -> participants.getChatRoom().getRoomId())
                .distinct()
                .collect(Collectors.toList());

        // 채팅방 ID로 채팅방 정보 조회 및 DTO 반환
        return roomId.stream()
                .map(chatRoomRepository::findByRoomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(chatRoom -> {
                    int unreadCount = countUnreadMessages(chatRoom.getRoomId(), nickName);
                    return convertToDTOWithUnreadCount(chatRoom, unreadCount);
                })
                .collect(Collectors.toList());
    }

    // 채팅방 나가기 및 채팅방 참여자 없을 시 채팅방 삭제
    public String leaveChatRoom(String roomId, String nickName) {
        // 채팅방 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 참여자 확인
        ChatParticipants participant = chatParticipantsRepository.findByChatRoomRoomIdAndUserNickname(roomId, nickName)
                .orElseThrow(() -> new RuntimeException("참여자가 채팅방에 존재하지 않습니다."));

        if (participant.getChatRole() == ChatRole.OWNER) {
            chatParticipantsRepository.findByChatRoomRoomId(roomId).stream()
                    .filter(p -> p.getChatRole() == ChatRole.MEMBER)
                    .findFirst()
                    .ifPresent(p -> {
                        p.setChatRole(ChatRole.OWNER);
                        chatParticipantsRepository.save(p);
                    });
        }

        // 참여자 제거
        chatParticipantsRepository.delete(participant);

        // 채팅방에 남은 참여자가 없으면 채팅방 사용
        List<ChatParticipants> checkParticipants = chatParticipantsRepository.findByChatRoomRoomId(roomId);
        if (checkParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
            chatMessageRepository.deleteAllByRoomId(roomId); // 메시지 삭제
            return "채팅방이 삭제되었습니다.";
        }
        return nickName+"님이 채팅방에서 나갔습니다.";
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // 개인 채팅방 생성
    private ChatRoomDTO createDirectChatRoom(User user1, User user2, String roomName) {
        String roomId = generatePersonalRoomId(user1.getNickname(), user2.getNickname());

        // 기존 채팅방 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomId(roomId);
        if (existingRoom.isPresent()) {
            return convertToDTO(existingRoom.get());
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomType(ChatRoomType.DM)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);

        // 참여자 추가
        chatParticipantsRepository.save(
                ChatParticipants.builder()
                        .chatRoom(chatRoom)
                        .user(user1)
                        .chatRole(ChatRole.OWNER) // 방장
                        .build());

        chatParticipantsRepository.save(
                ChatParticipants.builder()
                        .chatRoom(chatRoom)
                        .user(user2)
                        .chatRole(ChatRole.MEMBER) // 일반 멤버
                        .build());

        return convertToDTO(chatRoom);
    }

    // 그룹 채팅방 생성
    private ChatRoomDTO createGroupChatRoom(String roomName, List<User> users) {
        if(users.isEmpty()){
            throw new IllegalArgumentException("참여자는 최소 1명 이상이어야 합니다.");
        }

        String roomId = UUID.randomUUID().toString();

        // 새 그룹 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomType(ChatRoomType.GM)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);

        // 참여자 추가
        for(int i = 0 ; i < users.size(); i++){
            User user = users.get(i);

            ChatRole chatRole = (i == 0) ? ChatRole.OWNER : ChatRole.MEMBER;

            chatParticipantsRepository.save(
                    ChatParticipants.builder()
                            .chatRoom(chatRoom)
                            .user(user)
                            .chatRole(chatRole)
                            .build());
        }

        return convertToDTO(chatRoom);
    }

    // 두 사용자 ID를 정렬하여 고유한 roomId 생성
    private String generatePersonalRoomId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + ":" + user2 : user2 + ":" + user1;
    }

    // 특정 채팅방의 사용자가 읽지 않은 메시지 개수 계산
    private int countUnreadMessages(String roomId, String userId) {
        return (int) chatMessageRepository.findAllByRoomId(roomId).stream()
                .filter(message -> !message.getReaders().contains(userId))
                .count();
    }

    // ChatRoom Entity -> DTO
    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        // 참여자 정보를 추출 및 DTO 변환
        List<String> participantNickNames = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .map(chatParticipants -> chatParticipants.getUser().getNickname())
                .collect(Collectors.toList());

        // 방장 정보 추출 (OWNER 역할을 가진 참여자)
        String ownerId = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .filter(participant -> participant.getChatRole() == ChatRole.OWNER)
                .map(chatParticipants -> chatParticipants.getUser().getNickname())
                .findFirst()
                .orElse("Unknown");  // OWNER가 없으면 "Unknown"을 기본값으로 설정

        // 최근 메시지 조회
        ChatMessage lastMessage = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .ownerId(ownerId)
                .roomType(chatRoom.getRoomType())
                .createdAt(chatRoom.getCreatedAt())
                .participants(participantNickNames)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .build();
    }

    // ChatParticipants Entity -> DTO
    private ChatParticipantsDTO convertToParticipantsDTO(ChatParticipants participants, String roomId){
        return ChatParticipantsDTO.builder()
                .roomId(roomId)
                .user(participants.getUser().getNickname())
                .role(participants.getChatRole())
                .build();
    }

    // ChatRoom Entity -> DTO 변환 (읽지 않은 메시지 포함)
    private ChatRoomDTO convertToDTOWithUnreadCount(ChatRoom chatRoom, int unreadCount) {
        // 참여자 정보 추출
        List<String> participantUserNickNames = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .map(chatParticipants -> chatParticipants.getUser().getNickname())
                .collect(Collectors.toList());

        // 방장 정보 추출
        String ownerId = chatParticipantsRepository.findByChatRoomRoomId(chatRoom.getRoomId())
                .stream()
                .filter(participant -> participant.getChatRole() == ChatRole.OWNER)
                .map(chatParticipants -> chatParticipants.getUser().getNickname())
                .findFirst()
                .orElse("Unknown");

        // 최근 메시지 조회
        ChatMessage lastMessage = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .ownerId(ownerId)
                .roomType(chatRoom.getRoomType())
                .createdAt(chatRoom.getCreatedAt())
                .participants(participantUserNickNames)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .unreadCount(unreadCount) // 읽지 않은 메시지 개수 추가
                .build();
    }
}