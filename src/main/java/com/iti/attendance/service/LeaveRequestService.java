package com.iti.attendance.service;

import com.iti.attendance.model.LeaveRequest;
import com.iti.attendance.repository.LeaveRequestRepository;
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
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<LeaveRequest> findAllActive() {
        return leaveRequestRepository.findByDeletedFalse();
    }

    public List<LeaveRequest> findActiveByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdAndDeletedFalse(employeeId);
    }

    public Optional<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id).filter(l -> !l.isDeleted());
    }

    public LeaveRequest save(LeaveRequest request) {
        return leaveRequestRepository.save(request);
    }

    public void softDelete(Long id) {
        leaveRequestRepository.findById(id).ifPresent(request -> {
            request.setDeleted(true);
            leaveRequestRepository.save(request);
        });
    }

    public List<LeaveRequest> importFromExcel(MultipartFile file) throws IOException {
        List<LeaveRequest> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                LeaveRequest request = new LeaveRequest();
                request.setStartDate(LocalDate.parse(row.getCell(0).getStringCellValue()));
                request.setEndDate(LocalDate.parse(row.getCell(1).getStringCellValue()));
                request.setReason(row.getCell(2).getStringCellValue());
                request.setStatus(row.getCell(3).getStringCellValue());
                imported.add(leaveRequestRepository.save(request));
            }
        }
        return imported;
    }
}
