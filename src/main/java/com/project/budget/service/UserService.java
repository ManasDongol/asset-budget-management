package com.project.budget.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.budget.repository.UserRepository;
import com.project.budget.repository.StaffRepository;
import com.project.budget.entity.userEntity;
import com.project.budget.entity.StaffEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffRepository staffRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder; 
    
    
    public List<userEntity> viewUsers() {
        return userRepository.findAll();
    }
 

    // ✅ Add a new user with StaffEntity
    public userEntity addUser(String username, String password, String staffCode, String authoriser, String userStatus) {
        String encodedPassword = passwordEncoder.encode(password); // Hash password
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String inputer = auth.getName();

        // Fetch staff entity by code
        StaffEntity staff = staffRepository.findById(staffCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid staff code: " + staffCode));

        // Create new user
        userEntity user = new userEntity();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setStaff(staff);
        user.setInputer(inputer);
        user.setAuthoriser(authoriser);
        user.setUserStatus(userStatus);
        user.setCreatedAt(LocalDateTime.now()); // Save timestamp

        return userRepository.save(user);
    }

    // ✅ Get last 10 users
    public List<userEntity> getLast10Users() {
        return userRepository.findTop10ByOrderByIdDesc();
    }

    // ✅ Check if username exists
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    // ✅ Delete user by ID
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
    }
    
    // ✅ Find user by ID
    public Optional<userEntity> findById(Long id) {
        return userRepository.findById(id);
    }
    
    // ✅ Update existing user (overwrites createdAt with new time)
    public userEntity updateUser(userEntity updatedUser) {
        userEntity existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User with ID " + updatedUser.getId() + " not found"));

        // Update username
        existingUser.setUsername(updatedUser.getUsername());

        // Update staff if staffCode is provided
        if (updatedUser.getStaff() != null && updatedUser.getStaff().getStaffCode() != null) {
            StaffEntity staff = staffRepository.findById(updatedUser.getStaff().getStaffCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid staff code: " + updatedUser.getStaff().getStaffCode()));
            existingUser.setStaff(staff);
        }

        // Update authoriser
        existingUser.setAuthoriser(updatedUser.getAuthoriser());

        // Update inputer
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String inputer = auth.getName();
        existingUser.setInputer(inputer);

        // Update password only if new one provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(encodedPassword);
        }

        // ✅ Overwrite createdAt with current time
        existingUser.setCreatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }
}
