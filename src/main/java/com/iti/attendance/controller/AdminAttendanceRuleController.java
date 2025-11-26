package com.iti.attendance.controller;

import com.iti.attendance.model.AttendanceRule;
import com.iti.attendance.service.AttendanceRuleService;
import com.iti.attendance.service.BranchService;
import com.iti.attendance.service.DepartmentService;
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
@RequestMapping({"/admin/rules", "/employee/rules"})
public class AdminAttendanceRuleController {

    private final AttendanceRuleService attendanceRuleService;
    private final OrganizationService organizationService;
    private final BranchService branchService;
    private final DepartmentService departmentService;

    public AdminAttendanceRuleController(AttendanceRuleService attendanceRuleService,
                                         OrganizationService organizationService,
                                         BranchService branchService,
                                         DepartmentService departmentService) {
        this.attendanceRuleService = attendanceRuleService;
        this.organizationService = organizationService;
        this.branchService = branchService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rules", attendanceRuleService.findAllActive());
        return "admin-rules";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("rule", new AttendanceRule());
        attachLookups(model);
        return "admin-rule-form";
    }

    @PostMapping
    public String save(@ModelAttribute AttendanceRule rule) {
        attendanceRuleService.save(rule);
        return "redirect:/admin/rules";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        attendanceRuleService.findById(id).ifPresent(r -> model.addAttribute("rule", r));
        attachLookups(model);
        return "admin-rule-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        attendanceRuleService.softDelete(id);
        return "redirect:/admin/rules";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        attendanceRuleService.importFromExcel(file);
        return "redirect:/admin/rules";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rules");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("expectedCheckIn");
        header.createCell(2).setCellValue("expectedCheckOut");
        header.createCell(3).setCellValue("graceMinutes");
        header.createCell(4).setCellValue("overtimeAllowed");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-rule-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }

    private void attachLookups(Model model) {
        model.addAttribute("organizations", organizationService.findAllActive());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("departments", departmentService.findAllActive());
    }
}
