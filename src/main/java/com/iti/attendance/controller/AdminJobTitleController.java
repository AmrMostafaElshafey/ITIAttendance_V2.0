package com.iti.attendance.controller;

import com.iti.attendance.model.JobTitle;
import com.iti.attendance.service.JobTitleService;
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
@RequestMapping({"/admin/job-titles", "/employee/job-titles"})
public class AdminJobTitleController {

    private final JobTitleService jobTitleService;

    public AdminJobTitleController(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("jobTitles", jobTitleService.findAllActive());
        return "admin-job-titles";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("jobTitle", new JobTitle());
        return "admin-job-title-form";
    }

    @PostMapping
    public String save(@ModelAttribute JobTitle jobTitle) {
        jobTitleService.save(jobTitle);
        return "redirect:/admin/job-titles";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        jobTitleService.findById(id).ifPresent(title -> model.addAttribute("jobTitle", title));
        return "admin-job-title-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        jobTitleService.softDelete(id);
        return "redirect:/admin/job-titles";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        jobTitleService.importFromExcel(file);
        return "redirect:/admin/job-titles";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("JobTitles");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("description");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=job-title-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }
}
