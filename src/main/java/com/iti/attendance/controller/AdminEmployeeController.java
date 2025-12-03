package com.iti.attendance.controller;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.EmployeeStatus;
import com.iti.attendance.model.Role;
import com.iti.attendance.service.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','HR_EMPLOYEE')")
@Controller
@RequestMapping
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final BranchService branchService;
    private final JobTitleService jobTitleService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public AdminEmployeeController(EmployeeService employeeService, DepartmentService departmentService, BranchService branchService, JobTitleService jobTitleService, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.branchService = branchService;
        this.jobTitleService = jobTitleService;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping({"/admin/employees", "/employee/employees"})
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAllActive());
        return "admin-employees";
    }

    @GetMapping({"/admin/employees/new", "/employee/employees/new"})
    public String createForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("jobTitles", jobTitleService.findAllActive());
        model.addAttribute("managers", employeeService.findManagers());
        model.addAttribute("roles", Role.values());
        return "admin-employee-form";
    }

    @PostMapping({"/admin/employees", "/employee/employees"})
    public String save(@ModelAttribute Employee employee,
                       @RequestParam(value = "department.id", required = false) Long departmentId,
                       @RequestParam(value = "branch.id", required = false) Long branchId,
                       @RequestParam(value = "jobTitle.id", required = false) Long jobTitleId,
                       @RequestParam(value = "manager.id", required = false) Long managerId,
                       @RequestParam(value = "personalPhoto", required = false) MultipartFile personalPhoto,
                       @RequestParam(value = "nationalIdPhoto", required = false) MultipartFile nationalIdPhoto,
                       Model model) throws IOException {
        Employee existing = employee.getId() != null ? employeeService.findById(employee.getId()).orElse(null) : null;
        if (departmentId != null) {
            departmentService.findById(departmentId).ifPresent(employee::setDepartment);
        } else {
            employee.setDepartment(null);
        }

        if (branchId != null) {
            branchService.findById(branchId).ifPresent(employee::setBranch);
        } else {
            employee.setBranch(null);
        }

        if (jobTitleId != null) {
            jobTitleService.findById(jobTitleId).ifPresent(employee::setJobTitle);
        } else {
            employee.setJobTitle(null);
        }

        if (managerId != null) {
            employeeService.findById(managerId).ifPresent(employee::setManager);
        } else {
            employee.setManager(null);
        }

        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.PENDING);
        }
        String submittedPassword = employee.getPassword();
        if (submittedPassword == null || submittedPassword.isBlank()) {
            if (existing != null) {
                employee.setPassword(existing.getPassword());
            }
        } else if (!submittedPassword.startsWith("$2a$")) {
            employee.setPassword(passwordEncoder.encode(submittedPassword));
        }
        String personalPath = fileStorageService.storeFile(personalPhoto);
        if (personalPath != null) {
            employee.setPersonalPhotoPath(personalPath);
        }
        String nationalIdPath = fileStorageService.storeFile(nationalIdPhoto);
        if (nationalIdPath != null) {
            employee.setNationalIdPhotoPath(nationalIdPath);
        }
        try {
            employeeService.save(employee);
            return "redirect:/admin/employees";
        } catch (IllegalArgumentException ex) {
            return prepareForm(modelForError(model), employee, ex.getMessage());
        }
    }

    private Model modelForError(Model model) {
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("jobTitles", jobTitleService.findAllActive());
        model.addAttribute("managers", employeeService.findManagers());
        model.addAttribute("roles", Role.values());
        return model;
    }

    private String prepareForm(Model model, Employee employee, String errorMessage) {
        model.addAttribute("employee", employee);
        model.addAttribute("error", errorMessage);
        return "admin-employee-form";
    }

    @GetMapping({"/admin/employees/edit/{id}", "/employee/employees/edit/{id}"})
    public String edit(@PathVariable Long id, Model model) {
        employeeService.findById(id).ifPresent(emp -> model.addAttribute("employee", emp));
        model.addAttribute("departments", departmentService.findAllActive());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("jobTitles", jobTitleService.findAllActive());
        model.addAttribute("managers", employeeService.findManagers());
        model.addAttribute("roles", Role.values());
        return "admin-employee-form";
    }

    @PostMapping({"/admin/employees/delete/{id}", "/employee/employees/delete/{id}"})
    public String delete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return "redirect:/admin/employees";
    }

    @PostMapping({"/admin/employees/import", "/employee/employees/import"})
    public String bulkImport(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        List<Employee> imported = employeeService.importFromExcel(file);
        model.addAttribute("imported", imported);
        return "redirect:/admin/employees";
    }

    @GetMapping({"/admin/employees/template", "/employee/employees/template"})
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("email");
        header.createCell(2).setCellValue("phone");
        header.createCell(3).setCellValue("role");
        header.createCell(4).setCellValue("nationalId");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bos.toByteArray());
    }
}
