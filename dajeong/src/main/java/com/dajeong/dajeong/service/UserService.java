package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.LoginDTO;
import com.dajeong.dajeong.dto.SignupDTO;
import com.dajeong.dajeong.dto.UserlistDTO;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 전체 유저 목록 조회
    public List<UserlistDTO> getUserListDTO() {
        return userRepository.findAll().stream()
                .map(u -> UserlistDTO.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .name(u.getName())
                        .nationality(u.getNationality())
                        .age(u.getAge())
                        .region(u.getRegion())
                        .married(u.getMarried())
                        .hasChildren(u.getHasChildren())
                        .build()
                )
                .toList();
    }

    // 회원가입
    public void saveDTOUser(SignupDTO signupDTO) {
        if (userRepository.existsByUsername(signupDTO.getUsername())) {
            throw new IllegalArgumentException("이미 등록된 아이디입니다.");
        }
        User user = User.builder()
                .username(signupDTO.getUsername())
                .password(signupDTO.getPassword())
                .name(signupDTO.getName())
                .nationality(signupDTO.getNationality())
                .age(signupDTO.getAge())
                .region(signupDTO.getRegion())
                .married(signupDTO.getMarried())
                .hasChildren(signupDTO.getHasChildren())
                .build();
        userRepository.save(user);
    }

    // ID로 유저 DTO 조회
    public SignupDTO getUserDTOById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return SignupDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .password(u.getPassword())
                .name(u.getName())
                .nationality(u.getNationality())
                .age(u.getAge())
                .region(u.getRegion())
                .married(u.getMarried())
                .hasChildren(u.getHasChildren())
                .build();
    }

    // 로그인 시 User를 리턴
    public User authenticate(LoginDTO loginDTO) {
        return userRepository.findByUsername(loginDTO.getUsername())
                .filter(u -> u.getPassword().equals(loginDTO.getPassword()))
                .orElse(null);
    }
}