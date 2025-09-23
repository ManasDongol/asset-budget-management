package com.project.budget.controller;

import com.project.budget.service.FeaturesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    @Autowired
    private FeaturesService featuresService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); 
        model.addAttribute("username", username);

        // Load "Add User" feature status from DB
        int addUserStatus = featuresService.getFeatureStatus("Add User");
        model.addAttribute("addUserStatus", addUserStatus);

        return "dashboard";
    }

    @PostMapping("/dashboard/toggleAddUser")
    public String toggleAddUser(@RequestParam(value = "active", required = false) String active) {
        boolean isActive = (active != null && active.equals("true"));
        featuresService.updateFeatureStatus("Add User", isActive);

        return "redirect:/dashboard";
    }
}
