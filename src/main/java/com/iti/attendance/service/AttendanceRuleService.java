package com.iti.attendance.service;

import com.iti.attendance.model.AttendanceRule;
import com.iti.attendance.repository.AttendanceRuleRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceRuleService {

    private final AttendanceRuleRepository attendanceRuleRepository;

    public AttendanceRuleService(AttendanceRuleRepository attendanceRuleRepository) {
        this.attendanceRuleRepository = attendanceRuleRepository;
    }

    public List<AttendanceRule> findAllActive() {
        return attendanceRuleRepository.findByDeletedFalse();
    }

    public Optional<AttendanceRule> findById(Long id) {
        return attendanceRuleRepository.findById(id).filter(r -> !r.isDeleted());
    }

    public AttendanceRule save(AttendanceRule rule) {
        return attendanceRuleRepository.save(rule);
    }

    public void softDelete(Long id) {
        attendanceRuleRepository.findById(id).ifPresent(rule -> {
            rule.setDeleted(true);
            attendanceRuleRepository.save(rule);
        });
    }

    public List<AttendanceRule> importFromExcel(MultipartFile file) throws IOException {
        List<AttendanceRule> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                AttendanceRule rule = new AttendanceRule();
                rule.setName(row.getCell(0).getStringCellValue());
                rule.setExpectedCheckIn(LocalTime.parse(row.getCell(1).getStringCellValue()));
                rule.setExpectedCheckOut(LocalTime.parse(row.getCell(2).getStringCellValue()));
                rule.setGraceMinutes((int) row.getCell(3).getNumericCellValue());
                rule.setOvertimeAllowed(Boolean.parseBoolean(row.getCell(4).getStringCellValue()));
                imported.add(attendanceRuleRepository.save(rule));
            }
        }
        return imported;
    }
}
