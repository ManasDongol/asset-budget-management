package com.project.budget.controller;

import com.project.budget.entity.StaffEntity;
import com.project.budget.repository.StaffRepository;
import com.project.budget.service.OfficeLookupService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private OfficeLookupService officeLookupService;

    // View all staff or search
    @GetMapping("/all")
    public String viewAllStaff(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<StaffEntity> staffList;
        if (keyword != null && !keyword.isEmpty()) {
            staffList = staffRepository
                    .findByStaffCodeContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            keyword, keyword, keyword);
        } else {
            staffList = staffRepository.findAll();
        }
        model.addAttribute("staffList", staffList);
        model.addAttribute("keyword", keyword);
        return "staff-list";
    }

    // Show add staff form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("staff", new StaffEntity());
        model.addAttribute("recentStaff", staffRepository.findTop5ByOrderByCreatedAtDesc());
        return "staff-form";
    }

    // Handle adding new staff with office validation
    @PostMapping("/add")
    public String addStaff(@ModelAttribute("staff") StaffEntity staff, Model model) {
        String officeName = officeLookupService.getOfficeName(staff.getOfficeCode());
        if ("Unknown Office".equals(officeName)) {
            model.addAttribute("error", "Invalid office code: " + staff.getOfficeCode());
            model.addAttribute("recentStaff", staffRepository.findTop5ByOrderByCreatedAtDesc());
            return "staff-form";
        }

        staffRepository.save(staff);
        return "redirect:/staff/all";
    }

    // Show edit staff form
    @GetMapping("/edit/{staffCode}")
    public String showEditForm(@PathVariable("staffCode") String staffCode, Model model) {
        Optional<StaffEntity> staffOpt = staffRepository.findById(staffCode);
        if (staffOpt.isPresent()) {
            model.addAttribute("staff", staffOpt.get());
            model.addAttribute("recentStaff", staffRepository.findTop5ByOrderByCreatedAtDesc());
            return "staff-form"; 
        } else {
            return "redirect:/staff/all";
        }
    }

    // Handle update staff with office validation
    @PostMapping("/edit/{staffCode}")
    public String updateStaff(@PathVariable("staffCode") String staffCode,
                              @ModelAttribute("staff") StaffEntity updatedStaff,
                              Model model) {
        Optional<StaffEntity> staffOptional = staffRepository.findById(staffCode);
        if (staffOptional.isEmpty()) {
            model.addAttribute("error", "Staff not found.");
            return "staff-form";
        }

        String officeName = officeLookupService.getOfficeName(updatedStaff.getOfficeCode());
        if ("Unknown Office".equals(officeName)) {
            model.addAttribute("error", "Invalid office code: " + updatedStaff.getOfficeCode());
            StaffEntity staff = staffOptional.get();
            model.addAttribute("staff", staff);
            model.addAttribute("recentStaff", staffRepository.findTop5ByOrderByCreatedAtDesc());
            return "staff-form";
        }

        StaffEntity staff = staffOptional.get();
        staff.setFirstName(updatedStaff.getFirstName());
        staff.setLastName(updatedStaff.getLastName());
        staff.setOfficeCode(updatedStaff.getOfficeCode());
        staffRepository.save(staff);

        return "redirect:/staff/all";
    }

    // Show Delete Confirmation Page
    @GetMapping("/confirm-delete/{staffCode}")
    public String confirmDeleteStaff(@PathVariable("staffCode") String staffCode, Model model) {
        Optional<StaffEntity> staffOpt = staffRepository.findById(staffCode);
        if (staffOpt.isPresent()) {
            StaffEntity staff = staffOpt.get();
            model.addAttribute("staff", staff);
            model.addAttribute("officeName", officeLookupService.getOfficeName(staff.getOfficeCode())); 
            return "staff-delete-confirmation"; 
        } else {
            return "redirect:/staff?error=StaffNotFound";
        }
    }

    // Handle Deletion
    @GetMapping("/delete/{staffCode}")
    public String deleteStaff(@PathVariable("staffCode") String staffCode,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            staffRepository.deleteById(staffCode);
            redirectAttributes.addFlashAttribute("success", "Staff deleted successfully.");
            return "redirect:/staff/all";
        } catch (DataIntegrityViolationException ex) {
            Optional<StaffEntity> staffOpt = staffRepository.findById(staffCode);
            if (staffOpt.isPresent()) {
                StaffEntity staff = staffOpt.get();
                model.addAttribute("staff", staff);
                model.addAttribute("officeName", officeLookupService.getOfficeName(staff.getOfficeCode()));
                model.addAttribute("error", "Cannot delete staff: it is referenced by other records.");
                return "staff-delete-confirmation";
            } else {
                redirectAttributes.addFlashAttribute("error", "Staff not found.");
                return "redirect:/staff/all";
            }
        }
    }
    
    @GetMapping("/export")
    public void exportStaffToExcel(HttpServletResponse response) throws IOException {
        // Set content type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "staff_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Fetch staff data
        List<StaffEntity> staffList = staffRepository.findAll();

        // Create workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Staff");

            // Header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Staff Code");
            headerRow.createCell(1).setCellValue("First Name");
            headerRow.createCell(2).setCellValue("Last Name");
            headerRow.createCell(3).setCellValue("Office Code");
            headerRow.createCell(4).setCellValue("Branch Code");
            headerRow.createCell(5).setCellValue("Created At");

            // Data rows
            int rowCount = 1;
            for (StaffEntity staff : staffList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(staff.getStaffCode());
                row.createCell(1).setCellValue(staff.getFirstName());
                row.createCell(2).setCellValue(staff.getLastName());
                row.createCell(3).setCellValue(staff.getOfficeCode());
                row.createCell(4).setCellValue(staff.getBranch() != null ? staff.getBranch().getBranchCode() : "N/A");
                row.createCell(5).setCellValue(staff.getCreatedAt() != null ? staff.getCreatedAt().toString() : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to output stream
            workbook.write(outputStream);
        }
    }
}
