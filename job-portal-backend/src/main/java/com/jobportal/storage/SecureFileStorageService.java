package com.jobportal.storage;

import com.jobportal.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SecureFileStorageService {

    @Value("${storage.provider:LOCAL}")
    private String provider;

    private final LocalFileStorageService localFileStorageService;

    public SecureFileStorageService(LocalFileStorageService localFileStorageService) {
        this.localFileStorageService = localFileStorageService;
    }

    public String storeResume(MultipartFile file) {
        String chosen = provider == null ? "LOCAL" : provider.trim().toUpperCase();
        return switch (chosen) {
            case "LOCAL" -> localFileStorageService.storeResume(file);
            case "S3" -> throw new ApiException("S3 provider selected but not configured yet");
            default -> throw new ApiException("Unsupported storage provider: " + chosen);
        };
    }
}
