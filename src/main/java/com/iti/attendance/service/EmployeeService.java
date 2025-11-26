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

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void softDelete(Long id) {
        employeeRepository.findById(id).ifPresent(employee -> {
            employee.setDeleted(true);
            employeeRepository.save(employee);
        });
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmailAndDeletedFalse(email);
    }

    public List<Employee> findPendingEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.PENDING);
    }

    public List<Employee> findManagers() {
        return employeeRepository.findByRoleInAndDeletedFalse(List.of(Role.MANAGER, Role.BRANCH_MANAGER, Role.HR_MANAGER, Role.TRAINING_MANAGER));
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
                employee.setEmail(row.getCell(1).getStringCellValue());
                employee.setPhone(row.getCell(2).getStringCellValue());
                employee.setRole(Role.valueOf(row.getCell(3).getStringCellValue().toUpperCase()));
                employee.setNationalId(row.getCell(4).getStringCellValue());
                employee.setStatus(EmployeeStatus.PENDING);
                employee.setHireDate(LocalDate.now());
                imported.add(employeeRepository.save(employee));
            }
        }
        return imported;
    }
}
