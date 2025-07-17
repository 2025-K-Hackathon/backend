package com.dajeong.dajeong.service;

import org.springframework.stereotype.Service;
import com.dajeong.dajeong.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import com.dajeong.dajeong.dto.LoginDTO;
import com.dajeong.dajeong.dto.SignupDTO;
import com.dajeong.dajeong.dto.UserlistDTO;
import com.dajeong.dajeong.entity.User;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserlistDTO> getUserListDTO() {   // 유저 리스트 DTO 반환
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserlistDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
    } 

    public void saveDTOUser(SignupDTO SignupDTO) {    // 유저 등록
    if (userRepository.existsByUsername(SignupDTO.getUsername())) {
      throw new IllegalArgumentException("이미 등록된 아이디입니다.");
    }
    User user = User.builder()
            .username(SignupDTO.getUsername())
            .password(SignupDTO.getPassword())
            .name(SignupDTO.getName())
            .nationality(SignupDTO.getNationality())
            .age(SignupDTO.getAge())
            .region(SignupDTO.getRegion())
            .married(SignupDTO.getMarried())
            .hasChildren(SignupDTO.getHasChildren())
            .build();
    userRepository.save(user);
  }
  public SignupDTO getUserDTOById(Long id) {	// 유저 id로 DTO 찾기
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    return SignupDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .password(user.getPassword())
            .name(user.getName())
            .nationality(user.getNationality())
            .age(user.getAge())
            .region(user.getRegion())
            .married(user.getMarried())
            .hasChildren(user.getHasChildren())
            .build();
  }
  public boolean login(LoginDTO loginDTO) {
    Optional<User> user = userRepository.findByUsername(loginDTO.getUsername());
    if (user.isPresent()) {
        return user.get().getPassword().equals(loginDTO.getPassword());
    }
    return false;
}

}