package com.iti.attendance.controller;

import com.iti.attendance.model.LeaveRequest;
import com.iti.attendance.model.RequestType;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.LeaveRequestService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping({"/admin/leaves", "/employee/leaves"})
public class AdminLeaveController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;

    public AdminLeaveController(LeaveRequestService leaveRequestService, EmployeeService employeeService) {
        this.leaveRequestService = leaveRequestService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("leaves", leaveRequestService.findAllActive());
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("requestTypes", RequestType.values());
        model.addAttribute("basePath", resolveBasePath());
        return "admin-leaves";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("leaveRequest", new LeaveRequest());
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("requestTypes", RequestType.values());
        model.addAttribute("basePath", resolveBasePath());
        return "admin-leave-form";
    }

    @PostMapping
    public String save(@ModelAttribute LeaveRequest leaveRequest, @RequestParam("start") String start, @RequestParam("end") String end) {
        leaveRequest.setStartDate(LocalDate.parse(start));
        leaveRequest.setEndDate(LocalDate.parse(end));
        leaveRequestService.save(leaveRequest);
        return "redirect:" + resolveBasePath();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        leaveRequestService.findById(id).ifPresent(lr -> model.addAttribute("leaveRequest", lr));
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("requestTypes", RequestType.values());
        model.addAttribute("basePath", resolveBasePath());
        return "admin-leave-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        leaveRequestService.softDelete(id);
        return "redirect:" + resolveBasePath();
    }

    @PostMapping("/import")
    public String bulkImport(@RequestParam("file") MultipartFile file) throws IOException {
        leaveRequestService.importFromExcel(file);
        return "redirect:" + resolveBasePath();
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LeaveRequests");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("startDate");
        header.createCell(1).setCellValue("endDate");
        header.createCell(2).setCellValue("reason");
        header.createCell(3).setCellValue("status");
        header.createCell(4).setCellValue("type (LEAVE|MISSION|PERMIT)");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }

    private String resolveBasePath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean adminContext = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_ADMIN")
                            || auth.equals("ROLE_HR_MANAGER")
                            || auth.equals("ROLE_HR_EMPLOYEE"));
            if (adminContext) {
                return "/admin/leaves";
            }
        }
        return "/employee/leaves";
    }
}
