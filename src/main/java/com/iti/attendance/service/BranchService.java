package com.iti.attendance.service;

import com.iti.attendance.model.Branch;
import com.iti.attendance.repository.BranchRepository;
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
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> findAllActive() {
        return branchRepository.findByDeletedFalse();
    }

    public Optional<Branch> findById(Long id) {
        return branchRepository.findById(id).filter(b -> !b.isDeleted());
    }

    public Branch save(Branch branch) {
        return branchRepository.save(branch);
    }

    public void softDelete(Long id) {
        branchRepository.findById(id).ifPresent(branch -> {
            branch.setDeleted(true);
            branchRepository.save(branch);
        });
    }

    public List<Branch> importFromExcel(MultipartFile file) throws IOException {
        List<Branch> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                Branch branch = new Branch();
                branch.setName(row.getCell(0).getStringCellValue());
                branch.setLocation(row.getCell(1).getStringCellValue());
                imported.add(branchRepository.save(branch));
            }
        }
        return imported;
    }
}
