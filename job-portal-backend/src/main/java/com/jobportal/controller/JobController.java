package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.security.SecurityUtil;
import com.jobportal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/jobs", "/api/v1/jobs"})
@CrossOrigin(origins = "http://localhost:3000")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public ResponseEntity<?> addJob(@Valid @RequestBody JobRequest request) {
        var currentUser = SecurityUtil.currentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.addJob(request, currentUser.id()));
    }

    @GetMapping
    public Page<?> getJobs(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String location,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        return jobService.getJobs(keyword, location, page, size);
    }

    @GetMapping("/{id}")
    public Object getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @GetMapping("/employer/me")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Page<?> getMyJobs(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        var currentUser = SecurityUtil.currentUser();
        return jobService.getEmployerJobs(currentUser.id(), page, size);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        var currentUser = SecurityUtil.currentUser();
        return jobService.updateJob(id, request, currentUser.id(), currentUser.role());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public void deleteJob(@PathVariable Long id) {
        var currentUser = SecurityUtil.currentUser();
        jobService.deleteJob(id, currentUser.id(), currentUser.role());
    }
}
