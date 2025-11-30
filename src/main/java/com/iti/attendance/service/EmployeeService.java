package com.iti.attendance.service;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.EmployeeStatus;
import com.iti.attendance.model.Role;
import com.iti.attendance.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAllActive() {
        return employeeRepository.findByDeletedFalse();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id).filter(e -> !e.isDeleted());
    }

    public Employee getReference(Long id) {
        return employeeRepository.getReferenceById(id);
    }

    public Employee save(Employee employee) {
        normalizeEmail(employee);
        enforceUniqueEmail(employee);
        return employeeRepository.save(employee);
    }

    private void normalizeEmail(Employee employee) {
        if (employee.getEmail() != null) {
            employee.setEmail(employee.getEmail().trim().toLowerCase());
        }
    }

    private void enforceUniqueEmail(Employee employee) {
        if (employee.getEmail() == null || employee.getEmail().isBlank()) {
            return;
        }
        employeeRepository.findByEmailAndDeletedFalse(employee.getEmail())
                .filter(existing -> !existing.getId().equals(employee.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("البريد الإلكتروني مستخدم بالفعل لموظف آخر");
                });
    }

    public void softDelete(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
        });
    }

    public Optional<Employee> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return employeeRepository.findByEmailAndDeletedFalse(email.trim().toLowerCase());
    }

    public List<Employee> findPendingEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.PENDING);
    }

    public List<Employee> findManagers() {
        return employeeRepository.findByRoleInAndDeletedFalse(List.of(Role.MANAGER, Role.BRANCH_MANAGER, Role.HR_MANAGER));
    }

    public List<Employee> findByRoles(List<Role> roles) {
        return employeeRepository.findByRoleInAndDeletedFalse(roles);
    }

    public List<Employee> findByManager(Employee manager) {
        return employeeRepository.findByManagerAndDeletedFalse(manager);
    }

    public List<Employee> importFromExcel(MultipartFile file) throws IOException {
        List<Employee> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                Employee employee = new Employee();
                employee.setName(row.getCell(0).getStringCellValue());
                employee.setEmail(row.getCell(1).getStringCellValue().trim().toLowerCase());
                employee.setPhone(row.getCell(2).getStringCellValue());
                employee.setRole(Role.valueOf(row.getCell(3).getStringCellValue().toUpperCase()));
                employee.setNationalId(row.getCell(4).getStringCellValue());
                employee.setStatus(EmployeeStatus.PENDING);
                employee.setHireDate(LocalDate.now());
                if (!employeeRepository.existsByEmailAndDeletedFalse(employee.getEmail())) {
                    imported.add(employeeRepository.save(employee));
                }
            }
        }
        return imported;
    }
}
