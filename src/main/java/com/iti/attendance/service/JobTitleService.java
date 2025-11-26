package com.iti.attendance.service;

import com.iti.attendance.model.JobTitle;
import com.iti.attendance.repository.JobTitleRepository;
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
public class JobTitleService {

    private final JobTitleRepository jobTitleRepository;

    public JobTitleService(JobTitleRepository jobTitleRepository) {
        this.jobTitleRepository = jobTitleRepository;
    }

    public List<JobTitle> findAllActive() {
        return jobTitleRepository.findByDeletedFalse();
    }

    public Optional<JobTitle> findById(Long id) {
        return jobTitleRepository.findById(id).filter(j -> !j.isDeleted());
    }

    public JobTitle save(JobTitle jobTitle) {
        return jobTitleRepository.save(jobTitle);
    }

    public void softDelete(Long id) {
        jobTitleRepository.findById(id).ifPresent(title -> {
            title.setDeleted(true);
            jobTitleRepository.save(title);
        });
    }

    public List<JobTitle> importFromExcel(MultipartFile file) throws IOException {
        List<JobTitle> imported = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                JobTitle title = new JobTitle();
                title.setName(row.getCell(0).getStringCellValue());
                title.setDescription(row.getCell(1).getStringCellValue());
                imported.add(jobTitleRepository.save(title));
            }
        }
        return imported;
    }
}
