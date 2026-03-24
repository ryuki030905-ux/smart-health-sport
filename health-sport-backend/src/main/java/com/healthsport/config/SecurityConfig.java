package com.healthsport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthsport.security.JwtAuthFilter;
import com.healthsport.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离一般就不走 csrf 这套了
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        // 现在用的是 JWT，就不用服务端 session 了
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 这几个接口先放开，不登录也能调
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/exercise/dict").permitAll()
                        // AI 建议接口（需要登录，下方 anyRequest 接管）
                        .requestMatchers("/api/v1/ai/**").authenticated()
                        // 内部工具接口（仅限本机调用，放行由 Controller 自身 IP 检查兜底）
                        .requestMatchers("/internal/**").permitAll()
                        // 管理员接口单独卡一下角色
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            // 没登录或者 token 过期了，就回 401
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            String body = new ObjectMapper().writeValueAsString(
                                    Result.fail(401, "未登录或Token已过期")
                            );
                            response.getWriter().write(body);
                        })
                        .accessDeniedHandler((request, response, e) -> {
                            // 登录了但是权限不够，就回 403
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            String body = new ObjectMapper().writeValueAsString(
                                    Result.fail(403, "权限不足")
                            );
                            response.getWriter().write(body);
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
