package com.iti.attendance.controller;

import com.iti.attendance.model.ApprovalLevel;
import com.iti.attendance.model.RequestType;
import com.iti.attendance.model.Role;
import com.iti.attendance.service.ApprovalLevelService;
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
@RequestMapping({"/admin/approvals", "/employee/approvals"})
public class AdminApprovalLevelController {

    private final ApprovalLevelService approvalLevelService;

    public AdminApprovalLevelController(ApprovalLevelService approvalLevelService) {
        this.approvalLevelService = approvalLevelService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("levels", approvalLevelService.findAllActive());
        model.addAttribute("roles", Role.values());
        model.addAttribute("types", RequestType.values());
        return "admin-approvals";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("level", new ApprovalLevel());
        model.addAttribute("roles", Role.values());
        model.addAttribute("types", RequestType.values());
        return "admin-approval-form";
    }

    @PostMapping
    public String save(@ModelAttribute ApprovalLevel approvalLevel) {
        approvalLevelService.save(approvalLevel);
        return "redirect:/admin/approvals";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        approvalLevelService.findById(id).ifPresent(level -> model.addAttribute("level", level));
        model.addAttribute("roles", Role.values());
        model.addAttribute("types", RequestType.values());
        return "admin-approval-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        approvalLevelService.softDelete(id);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        approvalLevelService.importFromExcel(file);
        return "redirect:/admin/approvals";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ApprovalLevels");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("requestType");
        header.createCell(1).setCellValue("role");
        header.createCell(2).setCellValue("levelOrder");
        header.createCell(3).setCellValue("slaHours");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=approval-level-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }
}
