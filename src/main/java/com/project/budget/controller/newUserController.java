package com.project.budget.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.project.budget.entity.userEntity;
import com.project.budget.repository.UserRepository;

@Controller
public class newUserController {
	  private final UserRepository userRepository;
	    private final PasswordEncoder passwordEncoder;

	    public newUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
	        this.userRepository = userRepository;
	        this.passwordEncoder = passwordEncoder;
	    }

	    @GetMapping("/newUser")
	    public String setPasswordForm() {
	        return "newUser"; // Thymeleaf page
	    }

	    @PostMapping("/newUser")
	    public String savePassword(@RequestParam String newPassword,
	                               @RequestParam String confirmPassword,
	                               Authentication authentication, Model model) {

	        String username = authentication.getName();
	        userEntity user = userRepository.findByUsername(username);

	        String oldPasswordHash = user.getPassword();

	        // heck if new password is same as old password
	        if (passwordEncoder.matches(newPassword, oldPasswordHash)) {
	            model.addAttribute("error", "Please set a new password");
	            return "newUser"; // return password change page
	        }
	        
	        // Confirm password match
	        else if (!newPassword.equals(confirmPassword)) {
	            model.addAttribute("error", "Passwords do not match");
	            return "newUser";
	        }

	        // Validate password strength
	        else if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
	            model.addAttribute("error", "Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
	            return "newUser";
	        }
	        

	        // Encode and save new password
	        String hashedNewPassword = passwordEncoder.encode(newPassword);
	        user.setPassword(hashedNewPassword);
	        user.setUserStatus("activeUser"); // update status after setting password
	        userRepository.save(user);

	        return "redirect:/login?passwordChanged";
	    }


	}