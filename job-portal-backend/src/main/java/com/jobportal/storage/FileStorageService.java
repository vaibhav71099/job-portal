package com.jobportal.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeResume(MultipartFile file);
}
