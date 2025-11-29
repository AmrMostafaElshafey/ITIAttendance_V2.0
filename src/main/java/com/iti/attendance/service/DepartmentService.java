package com.iti.attendance.service;

import com.iti.attendance.model.Department;
import com.iti.attendance.repository.DepartmentRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> findAllActive() {
        return departmentRepository.findByDeletedFalse();
    }

    public List<Department> findByBranch(Long branchId) {
        if (branchId == null) {
            return List.of();
        }
        return departmentRepository.findByBranchIdAndDeletedFalse(branchId);
    }

    public List<Department> findByBranches(List<Long> branchIds) {
        if (branchIds == null || branchIds.isEmpty()) {
            return List.of();
        }
        return departmentRepository.findByBranchIdInAndDeletedFalse(branchIds);
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id).filter(d -> !d.isDeleted());
    }

    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    public void softDelete(Long id) {
        departmentRepository.findById(id).ifPresent(department -> {
            department.setDeleted(true);
            departmentRepository.save(department);
        });
    }

    public List<Department> importFromExcel(MultipartFile file) throws IOException {
        List<Department> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                Department department = new Department();
                department.setName(row.getCell(0).getStringCellValue());
                department.setDescription(row.getCell(1).getStringCellValue());
                imported.add(departmentRepository.save(department));
            }
        }
        return imported;
    }
}
