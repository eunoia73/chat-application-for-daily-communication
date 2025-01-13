package com.one.social_project.domain.user.oauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login/oauth2/")
public class OAuthController {

    @GetMapping("code/google")
    public ResponseEntity<String> google(@RequestParam("code")String code) {
        System.out.println(code);
        return ResponseEntity.ok(code);
    }
}
