package com.one.social_project.domain.friend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.one.social_project.domain.friend.status.FriendshipStatus;
import com.one.social_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore  // 무한 참조 방지
    private User user;

    private String userEmail;
    private String friendEmail;
    private FriendshipStatus status;
    private boolean isFrom;

    @Setter
    private Long counterpartId;

    public void acceptFriendshipRequest() {
        status = FriendshipStatus.ACCEPT;
    }
}