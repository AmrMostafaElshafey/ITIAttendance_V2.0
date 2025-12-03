package com.iti.attendance.service;

import com.iti.attendance.model.Organization;
import com.iti.attendance.repository.OrganizationRepository;
import com.iti.attendance.service.FileStorageService;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final FileStorageService fileStorageService;

    public OrganizationService(OrganizationRepository organizationRepository, FileStorageService fileStorageService) {
        this.organizationRepository = organizationRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Organization> findAllActive() {
        return organizationRepository.findByDeletedFalse();
    }

    public Optional<Organization> findById(Long id) {
        return organizationRepository.findById(id).filter(o -> !o.isDeleted());
    }

    public Organization save(Organization organization) {
        try {
            return saveWithLogo(organization, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save organization", e);
        }
    }

    public Organization saveWithLogo(Organization organization, MultipartFile logoFile) throws IOException {
        Organization existing = null;
        if (organization.getId() != null) {
            existing = organizationRepository.findById(organization.getId()).orElse(null);
        }

        String storedLogoPath = fileStorageService.storeFile(logoFile);
        if (storedLogoPath != null) {
            organization.setLogoPath(storedLogoPath);
        } else if (existing != null && organization.getLogoPath() == null) {
            organization.setLogoPath(existing.getLogoPath());
        }

        if (existing != null) {
            organization.setDeleted(existing.isDeleted());
        }

        return organizationRepository.save(organization);
    }

    public void softDelete(Long id) {
        organizationRepository.findById(id).ifPresent(org -> {
            org.setDeleted(true);
            organizationRepository.save(org);
        });
    }

    public List<Organization> importFromExcel(MultipartFile file) throws IOException {
        List<Organization> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                Organization org = new Organization();
                org.setName(row.getCell(0).getStringCellValue());
                org.setDescription(row.getCell(1).getStringCellValue());
                imported.add(organizationRepository.save(org));
            }
        }
        return imported;
    }
}
