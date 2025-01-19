package com.one.social_project.domain.search;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ChatSearchCondition {
    private String message;
    private String createdAtGoe; // 시작 날짜 (이후)
    private String createdAtLoe; // 끝 날짜 (이전)



//    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//    // 사용자로부터 받은 String을 LocalDateTime으로 변환
//    public void setCreatedAtGoe(String createdAtGoe) {
//        if (createdAtGoe != null) {
//            this.createdAtGoe = LocalDate.parse(createdAtGoe, FORMATTER).atStartOfDay();
//            System.out.println("createdAtGoe"+createdAtGoe);
//        }
//    }
//
//    public void setCreatedAtLoe(String createdAtLoe) {
//        if (createdAtLoe != null) {
//            this.createdAtLoe = LocalDate.parse(createdAtLoe, FORMATTER).atStartOfDay();
//            System.out.println("createdAtLoe" + createdAtLoe);
//        }
//    }
}
