package com.project.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.budget.repository.*;

@Service
public class OfficeLookupService {

    @Autowired
    private BranchRepository branchRepo;
    @Autowired
    private ProvinceRepository provinceRepo;
    @Autowired
    private HeadOfficeRepository headRepo;

    public String getOfficeName(String officeCode) {
        if (officeCode == null) return null;

        var branch = branchRepo.findByBranchCode(officeCode);
        if (branch != null) {
            return branch.getBranchName();
        }

        var province = provinceRepo.findByProvinceCode(officeCode);
        if (province != null) {
            return province.getProvinceName();
        }

        var head = headRepo.findByHeadofficeCode(officeCode);
        if (head != null) {
            return head.getHeadofficeName(); 
        }

        return "Unknown Office";
    }
}

