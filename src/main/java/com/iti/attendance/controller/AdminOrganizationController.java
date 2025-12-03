package com.iti.attendance.controller;

import com.iti.attendance.model.Organization;
import com.iti.attendance.service.OrganizationService;
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

@PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','HR_EMPLOYEE')")
@Controller
@RequestMapping({"/admin/organizations", "/employee/organizations"})
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    public AdminOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("organizations", organizationService.findAllActive());
        return "admin-organizations";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("organization", new Organization());
        return "admin-organization-form";
    }

    @PostMapping
    public String save(@ModelAttribute Organization organization,
                       @RequestParam(value = "logo", required = false) MultipartFile logo) throws IOException {
        organizationService.saveWithLogo(organization, logo);
        return "redirect:/admin/organizations";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        organizationService.findById(id).ifPresent(org -> model.addAttribute("organization", org));
        return "admin-organization-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        organizationService.softDelete(id);
        return "redirect:/admin/organizations";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        organizationService.importFromExcel(file);
        return "redirect:/admin/organizations";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Organizations");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("description");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=organization-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }
}
