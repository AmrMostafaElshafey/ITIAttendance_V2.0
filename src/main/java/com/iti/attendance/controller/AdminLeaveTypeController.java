package com.iti.attendance.controller;

import com.iti.attendance.model.LeaveType;
import com.iti.attendance.model.RequestType;
import com.iti.attendance.service.LeaveTypeService;
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
@RequestMapping({"/admin/leave-types", "/employee/leave-types"})
public class AdminLeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public AdminLeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("leaveTypes", leaveTypeService.findAllActive());
        model.addAttribute("requestTypes", RequestType.values());
        return "admin-leave-types";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("leaveType", new LeaveType());
        model.addAttribute("requestTypes", RequestType.values());
        return "admin-leave-type-form";
    }

    @PostMapping
    public String save(@ModelAttribute LeaveType leaveType) {
        leaveTypeService.save(leaveType);
        return "redirect:/admin/leave-types";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        leaveTypeService.findById(id).ifPresent(type -> model.addAttribute("leaveType", type));
        model.addAttribute("requestTypes", RequestType.values());
        return "admin-leave-type-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        leaveTypeService.softDelete(id);
        return "redirect:/admin/leave-types";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        leaveTypeService.importFromExcel(file);
        return "redirect:/admin/leave-types";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LeaveTypes");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("description");
        header.createCell(2).setCellValue("maxDaysPerRequest");
        header.createCell(3).setCellValue("requiresManagerApproval");
        header.createCell(4).setCellValue("requiresHrApproval");
        header.createCell(5).setCellValue("graceHours");
        header.createCell(6).setCellValue("requestType (LEAVE|MISSION|PERMIT)");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave-type-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }
}
