package com.one.social_project.domain.email.controller;

import com.one.social_project.domain.email.dto.EmailDto;
import com.one.social_project.domain.email.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;

    // 인증코드 메일 발송
    @PostMapping("/send")
    public String mailSend(@RequestBody EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        emailService.sendEmail(emailDto.getMail());
        return "인증코드가 발송되었습니다.";
    }

    // 인증코드 인증
    @PostMapping("/verify")
    public Boolean verify(@RequestBody EmailDto emailDto) {
        log.info("EmailController.verify()");
        return emailService.verifyEmailCode(emailDto.getMail(), emailDto.getVerifyCode());
    }
}
