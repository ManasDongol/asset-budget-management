package com.project.budget.controller;

import com.project.budget.entity.HeadOfficeEntity;
import com.project.budget.repository.HeadOfficeRepository;
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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/headoffice")
public class HeadOfficeController {

    @Autowired
    private HeadOfficeRepository headOfficeRepository;

    // View all head offices
    @GetMapping("/all")
    public String viewAllHeadOffices(Model model) {
        List<HeadOfficeEntity> headOffices = headOfficeRepository.findAll();
        model.addAttribute("recentHeadOffices", headOfficeRepository.findTop5ByOrderByCreatedAtDesc());
        model.addAttribute("headOffices", headOffices);
        return "headoffice-list";
    }

    // Show add head office form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("headOffice", new HeadOfficeEntity());
        return "headoffice-form";
    }

    // Handle add head office
    @PostMapping("/add")
    public String addHeadOffice(@ModelAttribute("headOffice") HeadOfficeEntity headOffice) {
        headOfficeRepository.save(headOffice);
        return "redirect:/headoffice/all";
    }

    // Show edit head office form
    @GetMapping("/edit/{headofficeCode}")
    public String showEditForm(@PathVariable("headofficeCode") String headofficeCode, Model model) {
        Optional<HeadOfficeEntity> headOffice = headOfficeRepository.findById(headofficeCode);
        if (headOffice.isPresent()) {
            model.addAttribute("headOffice", headOffice.get());
            model.addAttribute("recentHeadOffices", headOfficeRepository.findTop5ByOrderByCreatedAtDesc());
            return "headoffice-form";
        } else {
            return "redirect:/headoffice/all";
        }
    }

    // Handle update head office
    @PostMapping("/edit/{headofficeCode}")
    public String updateHeadOffice(@PathVariable("headofficeCode") String headofficeCode,
                                   @ModelAttribute("headOffice") HeadOfficeEntity updatedHeadOffice) {
        Optional<HeadOfficeEntity> headOfficeOptional = headOfficeRepository.findById(headofficeCode);
        if (headOfficeOptional.isPresent()) {
            HeadOfficeEntity headOffice = headOfficeOptional.get();
            headOffice.setHeadofficeName(updatedHeadOffice.getHeadofficeName());
            headOffice.setHeadofficeAddress(updatedHeadOffice.getHeadofficeAddress());
            headOfficeRepository.save(headOffice);
        }
        return "redirect:/headoffice/all";
    }
    
    // ✅ Show Delete Confirmation Page
    @GetMapping("/confirm-delete/{headofficeCode}")
    public String confirmDeleteHeadOffice(@PathVariable("headofficeCode") String headofficeCode,
                                          Model model) {
        Optional<HeadOfficeEntity> headOfficeOpt = headOfficeRepository.findById(headofficeCode);
        if (headOfficeOpt.isPresent()) {
            HeadOfficeEntity headOffice = headOfficeOpt.get();
            model.addAttribute("headoffice", headOffice);
            return "headoffice-delete-confirmation"; // Thymeleaf page
        } else {
            return "redirect:/headoffice/all?error=HeadOfficeNotFound";
        }
    }

    // ✅ Handle Deletion
    @GetMapping("/delete/{headofficeCode}")
    public String deleteHeadOffice(@PathVariable("headofficeCode") String headofficeCode,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            headOfficeRepository.deleteById(headofficeCode);
            redirectAttributes.addFlashAttribute("success", "Head Office deleted successfully.");
            return "redirect:/headoffice/all";
        } catch (DataIntegrityViolationException ex) {
            // FK violation -> show error on confirmation page
            Optional<HeadOfficeEntity> headOfficeOpt = headOfficeRepository.findById(headofficeCode);
            if (headOfficeOpt.isPresent()) {
                HeadOfficeEntity headOffice = headOfficeOpt.get();
                model.addAttribute("headoffice", headOffice);
                model.addAttribute("error", "Cannot delete Head Office: it is referenced by other records.");
                return "headoffice-delete-confirmation";
            } else {
                redirectAttributes.addFlashAttribute("error", "Head Office not found.");
                return "redirect:/headoffice/all";
            }
        }
    }
    
    @GetMapping("/export")
    public void exportHeadOfficesToExcel(HttpServletResponse response) throws IOException {
        // Set content type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "headoffices_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Fetch head office data
        List<HeadOfficeEntity> headOfficeList = headOfficeRepository.findAll();

        // Create workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Head Offices");

            // Header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Head Office Code");
            headerRow.createCell(1).setCellValue("Head Office Name");
            headerRow.createCell(2).setCellValue("Head Office Address");
            headerRow.createCell(3).setCellValue("Created At");

            // Data rows
            int rowCount = 1;
            for (HeadOfficeEntity ho : headOfficeList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(ho.getHeadofficeCode());
                row.createCell(1).setCellValue(ho.getHeadofficeName());
                row.createCell(2).setCellValue(ho.getHeadofficeAddress() != null ? ho.getHeadofficeAddress() : "N/A");
                row.createCell(3).setCellValue(ho.getCreatedAt() != null ? ho.getCreatedAt().toString() : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i <= 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write workbook to output stream
            workbook.write(outputStream);
        }
    }

}
