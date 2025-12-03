package com.iti.attendance.controller;

import com.iti.attendance.model.AccessPolicy;
import com.iti.attendance.model.Role;
import com.iti.attendance.service.AccessPolicyService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','HR_EMPLOYEE')")
@Controller
@RequestMapping({"/admin/access-policies", "/employee/access-policies"})
public class AdminAccessPolicyController {

    private final AccessPolicyService accessPolicyService;

    public AdminAccessPolicyController(AccessPolicyService accessPolicyService) {
        this.accessPolicyService = accessPolicyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("policies", accessPolicyService.findAllActive());
        model.addAttribute("roles", Role.values());
        model.addAttribute("endpoints", allEndpoints());
        return "admin-access-policies";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("policy", new AccessPolicy());
        model.addAttribute("roles", Role.values());
        model.addAttribute("endpoints", allEndpoints());
        return "admin-access-policy-form";
    }

    @PostMapping
    public String save(@ModelAttribute AccessPolicy accessPolicy) {
        accessPolicyService.save(accessPolicy);
        return "redirect:/admin/access-policies";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        accessPolicyService.findById(id).ifPresent(policy -> model.addAttribute("policy", policy));
        model.addAttribute("roles", Role.values());
        model.addAttribute("endpoints", allEndpoints());
        return "admin-access-policy-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        accessPolicyService.softDelete(id);
        return "redirect:/admin/access-policies";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        accessPolicyService.importFromExcel(file);
        return "redirect:/admin/access-policies";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("AccessPolicies");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("role");
        header.createCell(1).setCellValue("allowedPages");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=access-policy-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }

    private List<String> allEndpoints() {
        List<String> endpoints = new ArrayList<>();
        endpoints.add("/admin/dashboard");
        endpoints.add("/admin/organizations");
        endpoints.add("/admin/branches");
        endpoints.add("/admin/departments");
        endpoints.add("/admin/employees");
        endpoints.add("/admin/job-titles");
        endpoints.add("/admin/attendance-rules");
        endpoints.add("/admin/attendance");
        endpoints.add("/admin/leave-types");
        endpoints.add("/admin/leaves");
        endpoints.add("/admin/approvals");
        endpoints.add("/admin/access-policies");
        endpoints.add("/employee/portal");
        endpoints.add("/employee/attendance");
        endpoints.add("/employee/leaves");
        endpoints.add("/employee/approvals");
        endpoints.add("/employee/profile");
        return endpoints;
    }
}
