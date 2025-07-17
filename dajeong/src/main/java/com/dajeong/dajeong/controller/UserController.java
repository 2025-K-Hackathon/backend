package com.dajeong.dajeong.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.dajeong.dajeong.dto.LoginDTO;
import com.dajeong.dajeong.dto.SignupDTO;
import com.dajeong.dajeong.service.UserService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@Transactional
public class UserController {

    private final UserService userService;
    
    // 회원가입
    @PostMapping("/api/users/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupDTO signupDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("유효성 검사 실패");
        }
        try {
            userService.saveDTOUser(signupDTO);
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("에러: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserList() {
        return ResponseEntity.ok(userService.getUserListDTO());
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        boolean success = userService.login(loginDTO);
        if (success) {
            return ResponseEntity.ok("로그인 성공");
        } else {
            return ResponseEntity.status(401).body("로그인 실패: 아이디나 비밀번호가 틀렸습니다");
        }
    }

}