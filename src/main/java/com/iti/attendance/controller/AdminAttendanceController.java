package com.iti.attendance.controller;

import com.iti.attendance.model.AttendanceRecord;
import com.iti.attendance.service.AttendanceRecordService;
import com.iti.attendance.service.EmployeeService;
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
import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','HR_EMPLOYEE')")
@Controller
@RequestMapping({"/admin/attendance", "/employee/attendance"})
public class AdminAttendanceController {

    private final AttendanceRecordService attendanceRecordService;
    private final EmployeeService employeeService;

    public AdminAttendanceController(AttendanceRecordService attendanceRecordService, EmployeeService employeeService) {
        this.attendanceRecordService = attendanceRecordService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("records", attendanceRecordService.findAllActive());
        model.addAttribute("employees", employeeService.findAllActive());
        return "admin-attendance";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("attendanceRecord", new AttendanceRecord());
        model.addAttribute("employees", employeeService.findAllActive());
        return "admin-attendance-form";
    }

    @PostMapping
    public String save(@ModelAttribute AttendanceRecord record, @RequestParam("dateValue") String dateValue) {
        record.setDate(LocalDate.parse(dateValue));
        attendanceRecordService.save(record);
        return "redirect:/admin/attendance";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        attendanceRecordService.findById(id).ifPresent(rec -> model.addAttribute("attendanceRecord", rec));
        model.addAttribute("employees", employeeService.findAllActive());
        return "admin-attendance-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        attendanceRecordService.softDelete(id);
        return "redirect:/admin/attendance";
    }

    @PostMapping("/import")
    public String bulkImport(@RequestParam("file") MultipartFile file) throws IOException {
        attendanceRecordService.importFromExcel(file);
        return "redirect:/admin/attendance";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("date");
        header.createCell(1).setCellValue("status");
        header.createCell(2).setCellValue("checkIn");
        header.createCell(3).setCellValue("checkOut");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }
}
