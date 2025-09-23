package com.project.budget.controller;

import com.project.budget.entity.ProvinceEntity;
import com.project.budget.repository.ProvinceRepository;
import com.project.budget.service.OfficeLookupService;
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
@RequestMapping("/province")
public class ProvinceController {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private OfficeLookupService officeLookupService;

    // View all provinces
    @GetMapping("/all")
    public String viewAllProvinces(Model model) {
        List<ProvinceEntity> provinces = provinceRepository.findAll();
        model.addAttribute("provinces", provinces);
        model.addAttribute("recentProvinces", provinceRepository.findTop5ByOrderByCreatedAtDesc());
        return "province-list";
    }

    // Show add province form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("province", new ProvinceEntity());
        model.addAttribute("recentProvinces", provinceRepository.findTop5ByOrderByCreatedAtDesc());
        return "province-form";
    }

    // Handle add province with headOfficeCode validation
    @PostMapping("/add")
    public String addProvince(@ModelAttribute("province") ProvinceEntity province, Model model) {
        String officeName = officeLookupService.getOfficeName(province.getHeadOfficeCode());
        if ("Unknown Office".equals(officeName)) {
            model.addAttribute("error", "Invalid head office code: " + province.getHeadOfficeCode());
            model.addAttribute("recentProvinces", provinceRepository.findTop5ByOrderByCreatedAtDesc());
            return "province-form";
        }

        provinceRepository.save(province);
        return "redirect:/province/all";
    }

    // Show edit province form
    @GetMapping("/edit/{provinceCode}")
    public String showEditForm(@PathVariable("provinceCode") String provinceCode, Model model) {
        Optional<ProvinceEntity> provinceOpt = provinceRepository.findById(provinceCode);
        if (provinceOpt.isPresent()) {
            model.addAttribute("province", provinceOpt.get());
            model.addAttribute("recentProvinces", provinceRepository.findTop5ByOrderByCreatedAtDesc());
            return "province-form";
        } else {
            return "redirect:/province/all";
        }
    }

    // Handle update province with headOfficeCode validation
    @PostMapping("/edit/{provinceCode}")
    public String updateProvince(@PathVariable("provinceCode") String provinceCode,
                                 @ModelAttribute("province") ProvinceEntity updatedProvince,
                                 Model model) {
        Optional<ProvinceEntity> provinceOpt = provinceRepository.findById(provinceCode);
        if (provinceOpt.isEmpty()) {
            model.addAttribute("error", "Province not found.");
            return "province-form";
        }

        String officeName = officeLookupService.getOfficeName(updatedProvince.getHeadOfficeCode());
        if ("Unknown Office".equals(officeName)) {
            model.addAttribute("error", "Invalid head office code: " + updatedProvince.getHeadOfficeCode());
            ProvinceEntity province = provinceOpt.get();
            model.addAttribute("province", province);
            model.addAttribute("recentProvinces", provinceRepository.findTop5ByOrderByCreatedAtDesc());
            return "province-form";
        }

        ProvinceEntity province = provinceOpt.get();
        province.setProvinceName(updatedProvince.getProvinceName());
        province.setProvinceAddress(updatedProvince.getProvinceAddress());
        province.setHeadOfficeCode(updatedProvince.getHeadOfficeCode());
        provinceRepository.save(province);

        return "redirect:/province/all";
    }

    // Show Delete Confirmation Page
    @GetMapping("/confirm-delete/{provinceCode}")
    public String confirmDeleteProvince(@PathVariable("provinceCode") String provinceCode,
                                        Model model) {
        Optional<ProvinceEntity> provinceOpt = provinceRepository.findById(provinceCode);
        if (provinceOpt.isPresent()) {
            ProvinceEntity province = provinceOpt.get();
            model.addAttribute("province", province);
            model.addAttribute("headOfficeName", officeLookupService.getOfficeName(province.getHeadOfficeCode()));
            return "province-delete-confirmation";
        } else {
            return "redirect:/province/all?error=ProvinceNotFound";
        }
    }

    // Handle Deletion
    @GetMapping("/delete/{provinceCode}")
    public String deleteProvince(@PathVariable("provinceCode") String provinceCode,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            provinceRepository.deleteById(provinceCode);
            redirectAttributes.addFlashAttribute("success", "Province deleted successfully.");
            return "redirect:/province/all";
        } catch (DataIntegrityViolationException ex) {
            Optional<ProvinceEntity> provinceOpt = provinceRepository.findById(provinceCode);
            if (provinceOpt.isPresent()) {
                ProvinceEntity province = provinceOpt.get();
                model.addAttribute("province", province);
                model.addAttribute("headOfficeName", officeLookupService.getOfficeName(province.getHeadOfficeCode()));
                model.addAttribute("error", "Cannot delete province: it is referenced by other records.");
                return "province-delete-confirmation";
            } else {
                redirectAttributes.addFlashAttribute("error", "Province not found.");
                return "redirect:/province/all";
            }
        }
    }
    
    @GetMapping("/export")
    public void exportProvincesToExcel(HttpServletResponse response) throws IOException {
        // Set content type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "provinces_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Fetch province data
        List<ProvinceEntity> provinceList = provinceRepository.findAll();

        // Create workbook and sheet
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Provinces");

            // Header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Province Code");
            headerRow.createCell(1).setCellValue("Province Name");
            headerRow.createCell(2).setCellValue("Province Address");
            headerRow.createCell(3).setCellValue("Head Office Code");
            headerRow.createCell(4).setCellValue("Created At");

            // Data rows
            int rowCount = 1;
            for (ProvinceEntity province : provinceList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(province.getProvinceCode());
                row.createCell(1).setCellValue(province.getProvinceName());
                row.createCell(2).setCellValue(province.getProvinceAddress() != null ? province.getProvinceAddress() : "N/A");
                row.createCell(3).setCellValue(province.getHeadOfficeCode() != null ? province.getHeadOfficeCode() : "N/A");
                row.createCell(4).setCellValue(province.getCreatedAt() != null ? province.getCreatedAt().toString() : "N/A");
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
