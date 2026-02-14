package com.jobportal.service;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.exception.ApiException;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.storage.SecureFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;
    private final SecureFileStorageService fileStorageService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              NotificationService notificationService,
                              SecureFileStorageService fileStorageService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
    }

    public Application apply(ApplicationRequest request, Long candidateId) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new ApiException("Job not found"));

        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidateId)) {
            throw new ApiException("You have already applied to this job");
        }

        Application application = new Application();
        application.setJobId(job.getId());
        application.setCandidateId(candidateId);
        application.setResume(request.resume());
        application.setStatus("APPLIED");
        application.setAtsScore(calculateAtsScore(job, request.resume()));

        Application saved = applicationRepository.save(application);
        notificationService.create(
                job.getEmployerId(),
                "APPLICATION",
                "New application received on job #" + job.getId() + " (candidate #" + candidateId + ")"
        );
        return saved;
    }

    public List<Application> getCandidateApplications(Long candidateId) {
        return applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidateId);
    }

    public List<Application> getJobApplications(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application updateStatus(Long id, String status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Application not found"));
        application.setStatus(status);
        Application saved = applicationRepository.save(application);
        notificationService.create(
                application.getCandidateId(),
                "APPLICATION_STATUS",
                "Application #" + application.getId() + " status changed to " + status
        );
        return saved;
    }

    public List<Application> getPipeline(Long requesterId, String role, Long jobId, String status) {
        List<Application> source;

        if (jobId != null) {
            validateEmployerOwnership(jobId, requesterId, role);
            source = status == null || status.isBlank()
                    ? applicationRepository.findByJobId(jobId)
                    : applicationRepository.findByJobIdAndStatus(jobId, status.toUpperCase(Locale.ROOT));
        } else {
            if ("ADMIN".equals(role)) {
                source = applicationRepository.findAll();
            } else {
                List<Long> jobIds = jobRepository.findByEmployerId(requesterId).stream().map(Job::getId).toList();
                source = jobIds.stream()
                        .flatMap(id -> applicationRepository.findByJobId(id).stream())
                        .toList();
            }

            if (status != null && !status.isBlank()) {
                String safeStatus = status.toUpperCase(Locale.ROOT);
                source = source.stream()
                        .filter(app -> safeStatus.equals(app.getStatus()))
                        .toList();
            }
        }

        return source;
    }

    public Map<String, Object> getPipelineSummary(Long requesterId, String role, Long jobId) {
        List<Application> items = getPipeline(requesterId, role, jobId, null);
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("APPLIED", items.stream().filter(a -> "APPLIED".equals(a.getStatus())).count());
        byStatus.put("SHORTLISTED", items.stream().filter(a -> "SHORTLISTED".equals(a.getStatus())).count());
        byStatus.put("REJECTED", items.stream().filter(a -> "REJECTED".equals(a.getStatus())).count());
        byStatus.put("HIRED", items.stream().filter(a -> "HIRED".equals(a.getStatus())).count());

        double avgAts = items.stream()
                .filter(app -> app.getAtsScore() != null)
                .mapToInt(Application::getAtsScore)
                .average()
                .orElse(0.0);

        return Map.of(
                "totalApplications", items.size(),
                "averageAtsScore", Math.round(avgAts),
                "statusBreakdown", byStatus
        );
    }

    public String uploadResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Resume file is required");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!contentType.equalsIgnoreCase("application/pdf")) {
            throw new ApiException("Only PDF resumes are allowed");
        }

        return fileStorageService.storeResume(file);
    }

    private void validateEmployerOwnership(Long jobId, Long requesterId, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ApiException("Job not found"));
        if (!job.getEmployerId().equals(requesterId)) {
            throw new ApiException("Not allowed to access this pipeline");
        }
    }

    private int calculateAtsScore(Job job, String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return 0;
        }

        String jobText = (job.getTitle() == null ? "" : job.getTitle()) + " "
                + (job.getDescription() == null ? "" : job.getDescription());
        Set<String> jobKeywords = normalizeKeywords(jobText);
        Set<String> resumeKeywords = normalizeKeywords(resumeText);

        if (jobKeywords.isEmpty()) {
            return 0;
        }

        long matched = jobKeywords.stream().filter(resumeKeywords::contains).count();
        double ratio = (double) matched / jobKeywords.size();
        return (int) Math.max(0, Math.min(100, Math.round(ratio * 100)));
    }

    private Set<String> normalizeKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        return new HashSet<>(Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 3)
                .toList());
    }
}
