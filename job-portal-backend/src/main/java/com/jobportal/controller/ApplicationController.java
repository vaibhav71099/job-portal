package com.jobportal.controller;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.dto.UpdateApplicationStatusRequest;
import com.jobportal.security.SecurityUtil;
import com.jobportal.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/api/applications", "/api/v1/applications"})
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> apply(@Valid @RequestBody ApplicationRequest request) {
        var currentUser = SecurityUtil.currentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(request, currentUser.id()));
    }

    @PostMapping("/upload-resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Map<String, String> uploadResume(@RequestPart("file") MultipartFile file) {
        String path = applicationService.uploadResume(file);
        return Map.of("resumePath", path);
    }

    @GetMapping("/candidate/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Object getMyApplications() {
        var currentUser = SecurityUtil.currentUser();
        return applicationService.getCandidateApplications(currentUser.id());
    }

    @GetMapping("/job/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object getJobApplications(@PathVariable Long id) {
        return applicationService.getJobApplications(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Object getAllApplications() {
        return applicationService.getAllApplications();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object updateApplicationStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request.status());
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object getPipeline(@RequestParam(required = false) Long jobId,
                              @RequestParam(required = false) String status) {
        var currentUser = SecurityUtil.currentUser();
        return applicationService.getPipeline(currentUser.id(), currentUser.role(), jobId, status);
    }

    @GetMapping("/pipeline/summary")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object getPipelineSummary(@RequestParam(required = false) Long jobId) {
        var currentUser = SecurityUtil.currentUser();
        return applicationService.getPipelineSummary(currentUser.id(), currentUser.role(), jobId);
    }
}
