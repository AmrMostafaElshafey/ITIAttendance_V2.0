package com.iti.attendance.service;

import com.iti.attendance.model.AttendanceRecord;
import com.iti.attendance.repository.AttendanceRecordRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceRecordService {

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceRecordService(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    public List<AttendanceRecord> findAllActive() {
        return attendanceRecordRepository.findByDeletedFalse();
    }

    public Optional<AttendanceRecord> findById(Long id) {
        return attendanceRecordRepository.findById(id).filter(a -> !a.isDeleted());
    }

    public AttendanceRecord save(AttendanceRecord record) {
        return attendanceRecordRepository.save(record);
    }

    public void softDelete(Long id) {
        attendanceRecordRepository.findById(id).ifPresent(record -> {
            record.setDeleted(true);
            attendanceRecordRepository.save(record);
        });
    }

    public List<AttendanceRecord> findByEmployee(Long employeeId) {
        return attendanceRecordRepository.findByEmployeeIdAndDeletedFalse(employeeId);
    }

    public double calculateAttendancePercentage(Long employeeId) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .stream()
                .filter(record -> record.getDate() != null)
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            return 0.0;
        }

        long presentCount = records.stream()
                .filter(record -> record.getStatus() == null || record.getStatus().isBlank() || record.getStatus().equalsIgnoreCase("present") || record.getStatus().equalsIgnoreCase("حضور"))
                .count();

        return (presentCount * 100.0) / records.size();
    }

    public List<AttendanceRecord> importFromExcel(MultipartFile file) throws IOException {
        List<AttendanceRecord> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                AttendanceRecord record = new AttendanceRecord();
                record.setDate(LocalDate.parse(row.getCell(0).getStringCellValue()));
                record.setStatus(row.getCell(1).getStringCellValue());
                record.setCheckIn(LocalTime.parse(row.getCell(2).getStringCellValue()));
                record.setCheckOut(LocalTime.parse(row.getCell(3).getStringCellValue()));
                imported.add(attendanceRecordRepository.save(record));
            }
        }
        return imported;
    }
}
