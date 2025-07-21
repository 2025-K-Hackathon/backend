package com.dajeong.dajeong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // CSRF는 필요 없으니 끄고
      .csrf(csrf -> csrf.disable())
      // 어떤 요청이든 보안 필터에서 막지 않고 통과시킴
      .authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll()
      )
      // 폼 로그인, Basic, 로그아웃 전부 끔
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable())
      .logout(logout -> logout.disable());

    return http.build();
  }
}
