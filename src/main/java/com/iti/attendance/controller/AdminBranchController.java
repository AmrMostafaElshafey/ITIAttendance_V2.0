package com.iti.attendance.controller;

import com.iti.attendance.model.Branch;
import com.iti.attendance.service.BranchService;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.OrganizationService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping({"/admin/branches", "/employee/branches"})
public class AdminBranchController {

    private final BranchService branchService;
    private final OrganizationService organizationService;
    private final EmployeeService employeeService;

    public AdminBranchController(BranchService branchService, OrganizationService organizationService, EmployeeService employeeService) {
        this.branchService = branchService;
        this.organizationService = organizationService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", branchService.findAllActive());
        return "admin-branches";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("branch", new Branch());
        attachLookups(model);
        return "admin-branch-form";
    }

    @PostMapping
    public String save(@ModelAttribute Branch branch,
                       @RequestParam(value = "organization.id", required = false) Long organizationId,
                       @RequestParam(value = "manager.id", required = false) Long managerId) {
        branch.setOrganization(organizationId != null ? organizationService.findById(organizationId).orElse(null) : null);
        if (managerId != null) {
            branch.setManager(employeeService.findById(managerId).orElse(null));
        } else {
            branch.setManager(null);
        }
        branchService.save(branch);
        return "redirect:/admin/branches";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        branchService.findById(id).ifPresent(b -> model.addAttribute("branch", b));
        attachLookups(model);
        return "admin-branch-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        branchService.softDelete(id);
        return "redirect:/admin/branches";
    }

    @PostMapping("/import")
    public String bulkImport(@RequestParam("file") MultipartFile file) throws IOException {
        branchService.importFromExcel(file);
        return "redirect:/admin/branches";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Branches");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("location");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=branch-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }

    private void attachLookups(Model model) {
        model.addAttribute("organizations", organizationService.findAllActive());
        model.addAttribute("managers", employeeService.findManagers());
    }
}
