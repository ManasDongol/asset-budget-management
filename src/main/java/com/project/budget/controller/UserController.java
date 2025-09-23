package com.project.budget.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.budget.entity.userEntity;
import com.project.budget.repository.UserRepository;
import com.project.budget.service.FeaturesService;
import com.project.budget.service.OfficeLookupService;
import com.project.budget.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FeaturesService featuresService;
    
    @Autowired
    private UserRepository userRepository;

    
    
    // User page
    @GetMapping("/users")
    public String viewUsers(
            @RequestParam(value = "query", required = false) String query,
            Model model) {

        List<userEntity> users;
        if (query != null && !query.isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCase(query);
        } else {
            users = userRepository.findAll();
        }

        model.addAttribute("users", users);
        model.addAttribute("query", query); // pass the query back
        return "users";
    }

    @GetMapping("/adduser")
    public String showAddUserPage(Model model) {
        // Get logged-in username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedinUsername = auth.getName();

        model.addAttribute("loggedinUsername", loggedinUsername);
        model.addAttribute("recentUsers", userService.getLast10Users());

        // ✅ Use autowired instance, not static
        int addUserStatus = featuresService.getFeatureStatus("Add User");
        model.addAttribute("addUserStatus", addUserStatus);

        return "adduser";
    }

    
    //updatingu user
    @GetMapping("/adduser/{id}")
    public String UpdateUser(@PathVariable Long id, Model model) {
        Optional<userEntity> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            userEntity user = userOpt.get();
            model.addAttribute("user", user);
            return "adduser"; 
        }
        return "redirect:/adduser"; 

    }
    
    

    // Handle form submission
    @PostMapping("/adduser")
    public String addUser(
    		
    		
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("staffCode") String staffCode,
            @RequestParam("authoriser") String authoriser,
            String userStatus,
            Model model) {
    	
    	// if password is kept empty:
        if (password == null || password.trim().isEmpty()) {
    	    password = username+"@123";
    	    confirmPassword = password;
    	    userStatus = "newUser";
    	}
        else {
        	userStatus = "registeredUser";
        }
    		
        // Validate username (no special characters)
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            model.addAttribute("error", "Username can only contain letters and numbers");
        }
        // Validate password (min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char)
        else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            model.addAttribute("error", "Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
        }
        // Confirm password
        else if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
        }
        // Validate staff code (exactly 10 characters)
        else if (staffCode.length() > 10) {
            model.addAttribute("error", "Staff code must be less than 10 characters");
        }
        // Check if user name exists
        else if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username already exists");
        }
        else {
            // Save user to DB with all fields
            userService.addUser(username, password, staffCode, authoriser, userStatus);
            model.addAttribute("success", "User added successfully!");
        }

        // Refresh recent users
       
        model.addAttribute("recentUsers", userService.getLast10Users());
        return "adduser";
    }
   
    
    // Delete user
    @GetMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.deleteUserById(id);
            redirectAttrs.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Failed to delete user.");
        }
        return "redirect:/adduser";
    }
    
    // Viewing user
    @Autowired
    private OfficeLookupService officeLookupService;

    @GetMapping("/UserDeleteConfirmation/{id}")
    public String DeleteConfirmation(@PathVariable Long id, Model model) {
        Optional<userEntity> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            userEntity user = userOpt.get();

            // Fetch office name
            String officeName = officeLookupService.getOfficeName(user.getStaff().getOfficeCode());

            model.addAttribute("user", user);
            model.addAttribute("officeName", officeName); // pass office name to Thymeleaf

            return "UserDeleteConfirmation"; // the Thymeleaf page that shows full details
        }
        return "redirect:/adduser"; 
    }
    
    //updating user
    @PostMapping("/updatetheuser")
    public String updatetheUser(@ModelAttribute("user") userEntity user,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) { // to get logged-in user if stored in session
        try {
            // Update the user
            userService.updateUser(user);

            // Success message
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");




        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adduser";
    }
    
   
    @GetMapping("/updateuser/{id}")
    public String updateUser(@PathVariable("id") Long id, Model model) {
        // Fetch user from database
        Optional<userEntity> optionalUser = userRepository.findById(id);
        model.addAttribute("recentUsers", userService.getLast10Users());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedinUsername = auth.getName();
        model.addAttribute("loggedinUsername", loggedinUsername);


        if (optionalUser.isPresent()) {
            userEntity user = optionalUser.get();
            model.addAttribute("user", user);
            
           
            return "updateuser";
        } else {
            // User not found, redirect or show error
            return "redirect:/adduser?error=UserNotFound";
        }
    }

    @GetMapping("/users/export")
    public void exportUsersToExcel(HttpServletResponse response) throws IOException {
        // Set response type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Fetch all users from repository
        List<userEntity> userList = userRepository.findAll();
        
        // Create Excel workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Users");

            // Header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Username");
            headerRow.createCell(2).setCellValue("Staff Code");
            headerRow.createCell(3).setCellValue("Inputer");
            headerRow.createCell(4).setCellValue("Authoriser");
            headerRow.createCell(5).setCellValue("Created At");
            headerRow.createCell(6).setCellValue("User Status");

            // Data rows
            int rowCount = 1;
            for (userEntity user : userList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getStaff() != null ? user.getStaff().getStaffCode() : "N/A");
                row.createCell(3).setCellValue(user.getInputer());
                row.createCell(4).setCellValue(user.getAuthoriser());
                row.createCell(5).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A");
                row.createCell(6).setCellValue(user.getUserStatus());
            }

            // Auto-size columns
            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to output stream
            workbook.write(outputStream);
        }
    }
 

    
   

}
