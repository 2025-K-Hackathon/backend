package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.LoginDTO;
import com.dajeong.dajeong.dto.SignupDTO;
import com.dajeong.dajeong.dto.UserResponseDTO; 
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
                .childAge(signupDTO.getChildAge())
                .region(signupDTO.getRegion())
                .married(signupDTO.getMarried())
                .hasChildren(signupDTO.getHasChildren())
                .build();
        userRepository.save(user);
    }

    // ID로 유저 DTO 조회
    public SignupDTO getUserDTOById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return SignupDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .password(u.getPassword())
                .name(u.getName())
                .nationality(u.getNationality())
                .age(u.getAge())
                .childAge(u.getChildAge())
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

    // **내 정보 조회용**: 세션에서 꺼낸 User 엔티티를 DTO로 변환
    public UserResponseDTO getCurrentUserDTO(User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인된 사용자가 없습니다.");
        }
        return UserResponseDTO.fromEntity(user);
    }
}
