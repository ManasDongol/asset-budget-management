package com.project.budget.controller;

import com.project.budget.entity.BranchEntity;
import com.project.budget.entity.ProvinceEntity;
import com.project.budget.repository.BranchRepository;
import com.project.budget.repository.ProvinceRepository;



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
@RequestMapping("/branch")
public class BranchController {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    // View all branches
    @GetMapping("/all")
    public String viewAllBranches(Model model) {
        List<BranchEntity> branches = branchRepository.findAll();
        model.addAttribute("branches", branches);
        return "branch-list";
    }

    // Show add branch form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("branch", new BranchEntity());
        List<ProvinceEntity> provinces = provinceRepository.findAll();
        model.addAttribute("provinces", provinces);
        model.addAttribute("recentBranches", branchRepository.findTop5ByOrderByCreatedAtDesc());
        return "branch-form";
    }

    // Handle add branch with province validation
    @PostMapping("/add")
    public String addBranch(@ModelAttribute("branch") BranchEntity branch, Model model) {
    	if (!provinceRepository.existsById(branch.getProvinceCode().getProvinceCode())) {
            model.addAttribute("error", "Invalid province code: " + branch.getProvinceCode());
            List<ProvinceEntity> provinces = provinceRepository.findAll();
            model.addAttribute("provinces", provinces);
            model.addAttribute("recentBranches", branchRepository.findTop5ByOrderByCreatedAtDesc());
            return "branch-form";
        }

        branchRepository.save(branch);
        return "redirect:/branch/all";
    }

    // Show edit branch form
    @GetMapping("/edit/{branchCode}")
    public String showEditForm(@PathVariable("branchCode") String branchCode, Model model) {
        Optional<BranchEntity> branchOpt = branchRepository.findById(branchCode);
        if (branchOpt.isPresent()) {
            model.addAttribute("branch", branchOpt.get());
            List<ProvinceEntity> provinces = provinceRepository.findAll();
            model.addAttribute("provinces", provinces);
            model.addAttribute("recentBranches", branchRepository.findTop5ByOrderByCreatedAtDesc());
            return "branch-form";
        } else {
            return "redirect:/branch/all";
        }
    }

    // Handle update branch with province validation
    @PostMapping("/edit/{branchCode}")
    public String updateBranch(@PathVariable("branchCode") String branchCode,
                               @ModelAttribute("branch") BranchEntity updatedBranch,
                               Model model) {
        Optional<BranchEntity> branchOptional = branchRepository.findById(branchCode);
        if (branchOptional.isEmpty()) {
            model.addAttribute("error", "Branch not found.");
            return "branch-form";
        }

        if (!provinceRepository.existsById(updatedBranch.getProvinceCode().getProvinceCode())) {
            model.addAttribute("error", "Invalid province code: " + updatedBranch.getProvinceCode());
            BranchEntity branch = branchOptional.get();
            model.addAttribute("branch", branch);
            List<ProvinceEntity> provinces = provinceRepository.findAll();
            model.addAttribute("provinces", provinces);
            model.addAttribute("recentBranches", branchRepository.findTop5ByOrderByCreatedAtDesc());
            return "branch-form";
        }

        BranchEntity branch = branchOptional.get();
        branch.setBranchName(updatedBranch.getBranchName());
        branch.setBranchAddress(updatedBranch.getBranchAddress());
        branch.setProvinceCode(updatedBranch.getProvinceCode());
        branchRepository.save(branch);

        return "redirect:/branch/all";
    }

    // Show Delete Confirmation Page
    @GetMapping("/confirm-delete/{branchCode}")
    public String confirmDeleteBranch(@PathVariable("branchCode") String branchCode, Model model) {
        Optional<BranchEntity> branchOpt = branchRepository.findById(branchCode);
        if (branchOpt.isPresent()) {
            model.addAttribute("branch", branchOpt.get());
            return "branch-delete-confirmation"; // Thymeleaf page
        } else {
            return "redirect:/branch?error=BranchNotFound";
        }
    }

    // Handle Deletion
    @GetMapping("/delete/{branchCode}")
    public String deleteBranch(@PathVariable("branchCode") String branchCode,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            branchRepository.deleteById(branchCode);
            redirectAttributes.addFlashAttribute("success", "Branch deleted successfully.");
            return "redirect:/branch/all";
        } catch (DataIntegrityViolationException ex) {
            Optional<BranchEntity> branchOpt = branchRepository.findById(branchCode);
            if (branchOpt.isPresent()) {
                BranchEntity branch = branchOpt.get();
                model.addAttribute("branch", branch);
                model.addAttribute("error", "Cannot delete branch: it is referenced by other records.");
                return "branch-delete-confirmation";
            } else {
                redirectAttributes.addFlashAttribute("error", "Branch not found.");
                return "redirect:/branch/all";
            }
        }
    }
    
    @GetMapping("/export")
    public void exportBranchesToExcel(HttpServletResponse response) throws IOException {
        // Set content type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "branches_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Fetch branch data
        List<BranchEntity> branchList = branchRepository.findAll();

        // Create workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Branches");

            // Header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Branch Code");
            headerRow.createCell(1).setCellValue("Branch Name");
            headerRow.createCell(2).setCellValue("Branch Address");
            headerRow.createCell(3).setCellValue("Province Code");
            headerRow.createCell(4).setCellValue("Created At");

            // Data rows
            int rowCount = 1;
            for (BranchEntity branch : branchList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(branch.getBranchCode());
                row.createCell(1).setCellValue(branch.getBranchName());
                row.createCell(2).setCellValue(branch.getBranchAddress());
                row.createCell(3).setCellValue(branch.getProvinceCode().getProvinceCode());
                row.createCell(4).setCellValue(branch.getCreatedAt() != null ? branch.getCreatedAt().toString() : "N/A");
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
