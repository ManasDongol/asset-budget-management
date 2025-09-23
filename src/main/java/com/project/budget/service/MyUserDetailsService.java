package com.project.budget.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.project.budget.repository.UserRepository;
import com.project.budget.entity.userEntity;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        userEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Map userStatus -> Role
        String role = "activeUser".equals(user.getUserStatus()) ? "ROLE_ACTIVE" : "ROLE_NEW";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(() -> role)) // assign role
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}
