package com.project.budget.controller;

import com.project.budget.entity.ApplicationDetailsEntity;
import com.project.budget.entity.AssetHistoryEntity;
import com.project.budget.entity.FiscalEntity;
import com.project.budget.repository.ApplicationDetailsRepository;
import com.project.budget.repository.AssetHistoryRepository;
import com.project.budget.repository.BranchRepository;
import com.project.budget.repository.FiscalRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/asset-application")
public class AssetApplicationController {

    @Autowired
    private FiscalRepository fiscalRepository;

    @Autowired
    private ApplicationDetailsRepository applicationDetailsRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private AssetHistoryRepository assetHistoryRepository;

    // List all applications or search by number
    @GetMapping("/all")
    public String viewAllApplications(
            @RequestParam(value = "appNumber", required = false) String appNumber,
            Model model) {

        List<ApplicationDetailsEntity> applicationRecords;

        if (appNumber != null && !appNumber.isEmpty()) {
            applicationRecords = applicationDetailsRepository.findByApplicationNumberContaining(appNumber);
        } else {
            applicationRecords = applicationDetailsRepository.findAll();
        }

        model.addAttribute("applicationRecords", applicationRecords);
        model.addAttribute("appNumber", appNumber);
        return "asset-application-list";
    }

    // New application form
    @GetMapping("/new")
    public String newApplication(Model model) {

        ApplicationDetailsEntity application = new ApplicationDetailsEntity();

        LocalDate today = LocalDate.now();
        FiscalEntity currentFiscal = fiscalRepository.findByDate(today);
        if (currentFiscal == null) {
            model.addAttribute("errorMessage", "No fiscal year found for current date!");
            return "asset-application";
        }

        String fiscalYear = currentFiscal.getFiscalYear();
        List<String> appNumbers = applicationDetailsRepository.findAllApplicationNumbersByFiscalYear(fiscalYear);

        int nextNumber = 1;
        if (!appNumbers.isEmpty()) {
            int max = 0;
            for (String num : appNumbers) {
                try {
                    String[] parts = num.split("-");
                    int val = Integer.parseInt(parts[1]);
                    if (val > max) max = val;
                } catch (Exception ignored) {}
            }
            nextNumber = max + 1;
        }

        String formattedNumber = String.format("%03d", nextNumber);
        application.setApplicationNumber(fiscalYear + "-" + formattedNumber);
        application.setFiscalYear(fiscalYear);

        List<AssetHistoryEntity> assetHistories = new ArrayList<>();
        assetHistories.add(new AssetHistoryEntity());
        application.setAssetHistories(assetHistories);

        model.addAttribute("applicationDetails", application);
        model.addAttribute("branches", branchRepository.findAll());
        return "asset-application";
    }

    // Save or update application
    @PostMapping("/save")
    public String saveApplication(@ModelAttribute("applicationDetails") ApplicationDetailsEntity applicationDetails,
                                  Model model) {

        String errorMessage = null;
        String successMessage = null;

        // Validation
        if (applicationDetails.getApplicationNumber() == null || applicationDetails.getApplicationNumber().trim().isEmpty()) {
            errorMessage = "Application number is required.";
        } else if (applicationDetails.getToWhom() == null || applicationDetails.getToWhom().trim().isEmpty()) {
            errorMessage = "To Whom field is required.";
        }

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("branches", branchRepository.findAll());
            return "asset-application";
        }

        // Set fiscal year
        LocalDate today = LocalDate.now();
        FiscalEntity currentFiscal = fiscalRepository.findByDate(today);
        if (currentFiscal != null) {
            applicationDetails.setFiscalYear(currentFiscal.getFiscalYear());
        }

        // Set reference for asset histories
        if (applicationDetails.getAssetHistories() != null) {
            applicationDetails.getAssetHistories().forEach(item ->
                    item.setApplicationDetailsEntity(applicationDetails)
            );
        }

        // Save or update
        applicationDetailsRepository.save(applicationDetails);

        successMessage = "Application saved successfully!";
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("applicationDetails", applicationDetails);
        model.addAttribute("branches", branchRepository.findAll());

        return "asset-application"; // same page
    }

    // Edit existing application
    @GetMapping("/edit")
    public String editApplication(@RequestParam("appNumber") String applicationNumber, Model model) {
        if (applicationNumber == null || applicationNumber.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Please enter a valid application number.");
            return "asset-application";
        }

        ApplicationDetailsEntity application = applicationDetailsRepository.findById(applicationNumber).orElse(null);

        if (application == null) {
            model.addAttribute("errorMessage", "Application number '" + applicationNumber + "' not found!");
            return "asset-application";
        }

        // Ensure asset histories exist
        if (application.getAssetHistories() == null || application.getAssetHistories().isEmpty()) {
            application.setAssetHistories(new ArrayList<>());
            application.getAssetHistories().add(new AssetHistoryEntity());
        }

        model.addAttribute("applicationDetails", application);
        model.addAttribute("branches", branchRepository.findAll());

        return "asset-application";
    }

    // Fetch recent orders by branch
    @GetMapping("/recent-orders")
    @ResponseBody
    public List<AssetHistoryEntity> getRecentOrders(@RequestParam String branchCode) {
        return assetHistoryRepository.findByBranch_BranchCode(branchCode);
    }
}
