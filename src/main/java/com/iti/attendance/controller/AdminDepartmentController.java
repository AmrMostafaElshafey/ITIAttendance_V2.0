package com.iti.attendance.controller;

import com.iti.attendance.model.Department;
import com.iti.attendance.service.DepartmentService;
import com.iti.attendance.service.EmployeeService;
import com.iti.attendance.service.BranchService;
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
import java.util.List;

@Controller
@RequestMapping({"/admin/departments", "/employee/departments"})
public class AdminDepartmentController {

    private final DepartmentService departmentService;
    private final BranchService branchService;
    private final EmployeeService employeeService;

    public AdminDepartmentController(DepartmentService departmentService, BranchService branchService, EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.branchService = branchService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", departmentService.findAllActive());
        return "admin-departments";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("department", new Department());
        attachLookups(model);
        return "admin-department-form";
    }

    @PostMapping
    public String save(@ModelAttribute Department department,
                       @RequestParam(value = "branch.id", required = false) Long branchId,
                       @RequestParam(value = "manager.id", required = false) Long managerId) {
        department.setBranch(branchId != null ? branchService.findById(branchId).orElse(null) : null);
        department.setManager(managerId != null ? employeeService.getReference(managerId) : null);
        departmentService.save(department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        departmentService.findById(id).ifPresent(dep -> model.addAttribute("department", dep));
        attachLookups(model);
        return "admin-department-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        departmentService.softDelete(id);
        return "redirect:/admin/departments";
    }

    @PostMapping("/import")
    public String bulkImport(@RequestParam("file") MultipartFile file) throws IOException {
        departmentService.importFromExcel(file);
        return "redirect:/admin/departments";
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Departments");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("description");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }

    private void attachLookups(Model model) {
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("managers", employeeService.findManagers());
    }
}
