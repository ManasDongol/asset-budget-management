package com.project.budget.controller;

import com.project.budget.entity.ApplicationDetailsEntity;
import com.project.budget.entity.AssetHistoryEntity;
import com.project.budget.entity.BranchEntity;
import com.project.budget.entity.FiscalEntity;
import com.project.budget.repository.ApplicationDetailsRepository;
import com.project.budget.repository.AssetHistoryRepository;
import com.project.budget.repository.BranchRepository;
import com.project.budget.repository.FiscalRepository;
import com.project.budget.service.AssetHistoryService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    
    @Autowired
    private AssetHistoryService assetHistoryService;
    
 // Show delete confirmation page
    @GetMapping("/confirm-delete")
    public String confirmDeleteApplication(@RequestParam("id") String id, Model model) {
    	if(id==null) {
    		return "redirect:/asset-application/all?error=ApplicationNotFound";
    	}
    	System.out.println(id);
        Optional<ApplicationDetailsEntity> existingApp = applicationDetailsRepository.findById(id);
        
        if (existingApp.isPresent()) {
        		
            model.addAttribute("application", existingApp.get());
            return "application-delete-confirmation"; // Thymeleaf page
        } else {
            return "redirect:/asset-application/all?error=ApplicationNotFound";
        }
    }

    // Delete application by ID
    @PostMapping("/delete")
    public String deleteApplication(@RequestParam("id") String id, RedirectAttributes redirectAttributes) {
        Optional<ApplicationDetailsEntity> existingApp = applicationDetailsRepository.findById(id);
        
        System.out.println("lololo the id is");
        System.out.println(id);
        if (existingApp.isPresent()) {
            applicationDetailsRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Application with ID " + id + " deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Application with ID " + id + " not found.");
        }

        return "redirect:/asset-application/all";
    }



    // List all applications or search by number
    @GetMapping("/all")
    public String viewAllApplications(
            @RequestParam(value = "appNumber", required = false) String appNumber,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        List<ApplicationDetailsEntity> applicationRecords;

        if (appNumber != null && !appNumber.isEmpty()) {
            applicationRecords = applicationDetailsRepository
                    .findByApplicationNumberContainingIgnoreCase(appNumber);
        } else {
            applicationRecords = applicationDetailsRepository.findAll();
        }

        int pageSize = 10; // 10 records per page
        int totalRecords = applicationRecords.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalRecords);

        List<ApplicationDetailsEntity> pageRecords = applicationRecords.subList(fromIndex, toIndex);

        model.addAttribute("applicationRecords", pageRecords);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("appNumber", appNumber);

        return "asset-application-list";
    }


    @GetMapping("/new")
    public String newOrEditApplication(
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "appNumber", required = false) String appNumber,
            Model model) {

        ApplicationDetailsEntity application;
        List<BranchEntity> allBranches = branchRepository.findAll();

        if (appNumber != null && !appNumber.trim().isEmpty()) {
            // ---- Editing existing application ----
            application = applicationDetailsRepository.findById(appNumber).orElse(null);

            if (application == null) {
                model.addAttribute("errorMessage", "Application number '" + appNumber + "' not found!");
                application = new ApplicationDetailsEntity();
            }
        } else {
            // ---- Creating new application ----
            application = new ApplicationDetailsEntity();

            LocalDate today = LocalDate.now();
            FiscalEntity currentFiscal = fiscalRepository.findByDate(today);

            if (currentFiscal == null) {
                model.addAttribute("errorMessage", "No fiscal year found for current date!");
            } else {
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
                    nextNumber = max + 10000;
                }

                // Generate ID with fiscal year, remove slashes for DB safety
                String sanitizedFiscalYear = fiscalYear.replace("/", "");
                String generatedNumber = sanitizedFiscalYear + "-" + String.format("%04d", nextNumber);

                application.setApplicationNumber(generatedNumber);
                application.setFiscalYear(fiscalYear);
                application.setFromWhom("सूचना प्रविधि विभाग");
                application.setStaffPost("विभागीय प्रमुख");
                application.setStaffCode("५५३९");
                application.setStaffFullName("गिरी राज रेग्मी");
            }
        }

        // Initialize asset histories if null
        if (application.getAssetHistories() == null || application.getAssetHistories().isEmpty()) {
            application.setAssetHistories(new ArrayList<>(List.of(new AssetHistoryEntity())));
        } else {
            application.getAssetHistories().forEach(asset -> {
                if (asset.getBranch() == null) {
                    asset.setBranch(new BranchEntity());
                }
            });
        }

        model.addAttribute("applicationDetails", application);
        model.addAttribute("branches", allBranches);

        if ("true".equals(success)) {
            model.addAttribute("successMessage", "Application saved successfully!");
        }

        return "asset-application";
    }


 // Save or update application
    @PostMapping("/save")
    public String saveApplication(
            @ModelAttribute("applicationDetails") ApplicationDetailsEntity applicationDetails,
            Model model) {

        // Validate required fields
        if (applicationDetails.getApplicationNumber() == null || applicationDetails.getApplicationNumber().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Application number is required.");
            model.addAttribute("branches", branchRepository.findAll());
            return "asset-application";
        }

        if (applicationDetails.getToWhom() == null || applicationDetails.getToWhom().trim().isEmpty()) {
            model.addAttribute("errorMessage", "To Whom field is required.");
            model.addAttribute("branches", branchRepository.findAll());
            return "asset-application";
        }

        // Set fiscal year
        LocalDate today = LocalDate.now();
        FiscalEntity currentFiscal = fiscalRepository.findByDate(today);
        if (currentFiscal != null) {
            applicationDetails.setFiscalYear(currentFiscal.getFiscalYear());

            // Prefix fiscal year if not already present
            if (!applicationDetails.getApplicationNumber().startsWith(currentFiscal.getFiscalYear() + "-")) {
                applicationDetails.setApplicationNumber(currentFiscal.getFiscalYear() + "-" + applicationDetails.getApplicationNumber());
            }
        }

        // Ensure assetHistories are linked to this application
        if (applicationDetails.getAssetHistories() != null) {
            applicationDetails.getAssetHistories()
                    .forEach(asset -> asset.setApplicationDetailsEntity(applicationDetails));
        }

        // Save to DB (handles new and existing automatically)
        applicationDetailsRepository.save(applicationDetails);

        // Redirect to avoid duplicate form submissions
        return "redirect:/asset-application/new?success=true&appNumber=" + applicationDetails.getApplicationNumber();
    }



   
    // Fetch recent orders by branch
    @GetMapping("/search")
    public String searchAssetsByBranch(@RequestParam("branchCode") String branchCode, Model model) {
        List<AssetHistoryEntity> assets = assetHistoryService.getByBranchCode(branchCode);
        model.addAttribute("assets", assets);
        return "fragments/asset-table :: assetTableBodyFragment";
    }
    
    @GetMapping("/export")
    public void exportBranchesToExcel(HttpServletResponse response) throws IOException {
        // Set content type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "applications_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Fetch branch data
        List<ApplicationDetailsEntity> applicationlist = applicationDetailsRepository.findAll();

        // Create workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Branches");

            // Header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Application Number");
            headerRow.createCell(1).setCellValue("Fiscal Year");
            headerRow.createCell(2).setCellValue("To Whom");
            headerRow.createCell(3).setCellValue("Date");
            headerRow.createCell(4).setCellValue("Subject");
            headerRow.createCell(5).setCellValue("Staff Name");

            // Data rows
            int rowCount = 1;
            for (ApplicationDetailsEntity application : applicationlist) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(application.getApplicationNumber());
                row.createCell(1).setCellValue(application.getFiscalYear());
                row.createCell(2).setCellValue(application.getToWhom());
                row.createCell(3).setCellValue(application.getDate());
                row.createCell(4).setCellValue(application.getSubject());
                row.createCell(5).setCellValue(application.getStaffFullName());
            }

            // Auto-size columns
            for (int i = 0; i <= 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to output stream
            workbook.write(outputStream);
        }
    }

}
