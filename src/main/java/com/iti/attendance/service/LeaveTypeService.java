package com.iti.attendance.service;

import com.iti.attendance.model.LeaveType;
import com.iti.attendance.repository.LeaveTypeRepository;
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
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    public List<LeaveType> findAllActive() {
        return leaveTypeRepository.findByDeletedFalse();
    }

    public Optional<LeaveType> findById(Long id) {
        return leaveTypeRepository.findById(id).filter(l -> !l.isDeleted());
    }

    public LeaveType save(LeaveType leaveType) {
        return leaveTypeRepository.save(leaveType);
    }

    public void softDelete(Long id) {
        leaveTypeRepository.findById(id).ifPresent(type -> {
            type.setDeleted(true);
            leaveTypeRepository.save(type);
        });
    }

    public List<LeaveType> importFromExcel(MultipartFile file) throws IOException {
        List<LeaveType> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                LeaveType type = new LeaveType();
                type.setName(row.getCell(0).getStringCellValue());
                type.setDescription(row.getCell(1).getStringCellValue());
                type.setMaxDaysPerRequest((int) row.getCell(2).getNumericCellValue());
                type.setRequiresManagerApproval(Boolean.parseBoolean(row.getCell(3).getStringCellValue()));
                type.setRequiresHrApproval(Boolean.parseBoolean(row.getCell(4).getStringCellValue()));
                imported.add(leaveTypeRepository.save(type));
            }
        }
        return imported;
    }
}
