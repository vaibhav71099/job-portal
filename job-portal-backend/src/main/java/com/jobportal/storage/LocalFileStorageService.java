package com.jobportal.storage;

import com.jobportal.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${storage.resume-dir}")
    private String resumeDir;

    @Override
    public String storeResume(MultipartFile file) {
        try {
            Path dirPath = Paths.get(resumeDir).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            String fileName = UUID.randomUUID() + ".pdf";
            Path filePath = dirPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/resumes/" + fileName;
        } catch (IOException ex) {
            throw new ApiException("Failed to upload resume");
        }
    }
}
