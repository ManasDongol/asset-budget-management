package com.project.budget.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.project.budget.entity.userEntity;
import com.project.budget.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomAuthSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        userEntity user = userRepository.findByUsername(username);

        if ("newUser".equals(user.getUserStatus())) {
            response.sendRedirect("/newUser");  // force set new password
        } else {
            response.sendRedirect("/dashboard");  // normal redirect
        }
    }
}

