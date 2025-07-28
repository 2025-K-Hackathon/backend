package com.dajeong.dajeong.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.dajeong.dajeong.entity.User;

import com.dajeong.dajeong.dto.SignupDTO;
import com.dajeong.dajeong.dto.LoginDTO;
import com.dajeong.dajeong.service.UserService;
import com.dajeong.dajeong.dto.UserResponseDTO;

@RestController
@Transactional
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    // 내 정보 조회
    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponseDTO> getMyInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "로그인이 필요합니다"
            );
        }
        UserResponseDTO dto = userService.getCurrentUserDTO(user);
        return ResponseEntity.ok(dto);
    }

    // 로그인
    @PostMapping("/api/users/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        var user = userService.authenticate(loginDTO); // 로그인 성공 시 User 리턴
        if (user != null) {
            session.setAttribute("user", user);
            return ResponseEntity.ok("로그인 성공");
        } else {
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }
}
