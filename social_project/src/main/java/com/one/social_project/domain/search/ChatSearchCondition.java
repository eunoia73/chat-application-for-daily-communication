package com.one.social_project.domain.search;

import lombok.Data;
@Data
public class ChatSearchCondition {
    private String message;
    private String createdAtGoe; // 시작 날짜 (이후)
    private String createdAtLoe; // 끝 날짜 (이전)

}
