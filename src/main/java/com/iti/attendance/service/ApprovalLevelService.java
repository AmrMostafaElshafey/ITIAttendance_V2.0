package com.iti.attendance.service;

import com.iti.attendance.model.ApprovalLevel;
import com.iti.attendance.repository.ApprovalLevelRepository;
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
public class ApprovalLevelService {

    private final ApprovalLevelRepository approvalLevelRepository;

    public ApprovalLevelService(ApprovalLevelRepository approvalLevelRepository) {
        this.approvalLevelRepository = approvalLevelRepository;
    }

    public List<ApprovalLevel> findAllActive() {
        return approvalLevelRepository.findByDeletedFalseOrderByLevelOrderAsc();
    }

    public Optional<ApprovalLevel> findById(Long id) {
        return approvalLevelRepository.findById(id).filter(a -> !a.isDeleted());
    }

    public ApprovalLevel save(ApprovalLevel approvalLevel) {
        return approvalLevelRepository.save(approvalLevel);
    }

    public void softDelete(Long id) {
        approvalLevelRepository.findById(id).ifPresent(level -> {
            level.setDeleted(true);
            approvalLevelRepository.save(level);
        });
    }

    public List<ApprovalLevel> importFromExcel(MultipartFile file) throws IOException {
        List<ApprovalLevel> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                ApprovalLevel level = new ApprovalLevel();
                level.setRequestType(com.iti.attendance.model.RequestType.valueOf(row.getCell(0).getStringCellValue().toUpperCase()));
                level.setRole(com.iti.attendance.model.Role.valueOf(row.getCell(1).getStringCellValue().toUpperCase()));
                level.setLevelOrder((int) row.getCell(2).getNumericCellValue());
                level.setSlaHours((int) row.getCell(3).getNumericCellValue());
                imported.add(approvalLevelRepository.save(level));
            }
        }
        return imported;
    }
}
