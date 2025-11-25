package com.iti.attendance.service;

import com.iti.attendance.model.AccessPolicy;
import com.iti.attendance.repository.AccessPolicyRepository;
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
public class AccessPolicyService {

    private final AccessPolicyRepository accessPolicyRepository;

    public AccessPolicyService(AccessPolicyRepository accessPolicyRepository) {
        this.accessPolicyRepository = accessPolicyRepository;
    }

    public List<AccessPolicy> findAllActive() {
        return accessPolicyRepository.findByDeletedFalse();
    }

    public Optional<AccessPolicy> findById(Long id) {
        return accessPolicyRepository.findById(id).filter(p -> !p.isDeleted());
    }

    public AccessPolicy save(AccessPolicy policy) {
        return accessPolicyRepository.save(policy);
    }

    public void softDelete(Long id) {
        accessPolicyRepository.findById(id).ifPresent(policy -> {
            policy.setDeleted(true);
            accessPolicyRepository.save(policy);
        });
    }

    public List<AccessPolicy> importFromExcel(MultipartFile file) throws IOException {
        List<AccessPolicy> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                AccessPolicy policy = new AccessPolicy();
                policy.setRole(com.iti.attendance.model.Role.valueOf(row.getCell(0).getStringCellValue().toUpperCase()));
                policy.setAllowedPages(row.getCell(1).getStringCellValue());
                imported.add(accessPolicyRepository.save(policy));
            }
        }
        return imported;
    }
}
