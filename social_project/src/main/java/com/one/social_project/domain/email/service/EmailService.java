package com.one.social_project.domain.email.service;

import com.one.social_project.domain.email.dto.util.ResultDto;
import com.one.social_project.domain.email.util.EmailRedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailRedisUtil redisUtil;
    private static final String senderEmail = "sanbyul1@naver.com";

    private String createCode() {
        int leftLimit = 48; // number '0'
        int rightLimit = 122; // alphabet 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 | i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    // 이메일 내용 초기화
    private String setContext(String code) {
        Context context = new Context();
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        context.setVariable("code", code);

        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine.process("mail", context);
    }

    // 이메일 폼 생성
    private MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = createCode();
        System.out.println(authCode);

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요. 인증번호입니다.");
        message.setFrom(senderEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        // Redis 에 해당 인증코드 인증 시간 설정
        redisUtil.setDataExpire(email, authCode, 60 * 30L);

        return message;
    }

    // 인증코드 이메일 발송
    public void sendEmail(String toEmail) throws MessagingException {
        System.out.println(toEmail);
        System.out.println(redisUtil.existData(toEmail));
        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }
        // 이메일 폼 생성
        MimeMessage emailForm = createEmailForm(toEmail);
        System.out.println(emailForm);
        // 이메일 발송
        javaMailSender.send(emailForm);
    }

    // 코드 검증
    public ResultDto verifyEmailCode(String email, String code) {
        String codeFoundByEmail = redisUtil.getData(email);
        log.info("code found by email: " + codeFoundByEmail);
        ResultDto resultDto = new ResultDto();
        resultDto.setResult(false);
        if (codeFoundByEmail == null) {
            return resultDto;
        }
        resultDto.setResult(codeFoundByEmail.equals(code));
        return resultDto;
    }
}
