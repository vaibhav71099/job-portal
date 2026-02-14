package com.jobportal.service;

import com.jobportal.model.Job;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public AnalyticsService(UserRepository userRepository,
                            JobRepository jobRepository,
                            ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public Map<String, Object> getOverview(Long requesterId, String role) {
        if ("ADMIN".equals(role)) {
            return adminOverview();
        }
        return employerOverview(requesterId);
    }

    private Map<String, Object> adminOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userRepository.count());
        data.put("totalCandidates", userRepository.countByRole("CANDIDATE"));
        data.put("totalEmployers", userRepository.countByRole("EMPLOYER"));
        data.put("totalJobs", jobRepository.count());
        data.put("totalApplications", applicationRepository.count());
        data.put("hired", applicationRepository.countByStatus("HIRED"));
        data.put("shortlisted", applicationRepository.countByStatus("SHORTLISTED"));
        data.put("rejected", applicationRepository.countByStatus("REJECTED"));
        data.put("applied", applicationRepository.countByStatus("APPLIED"));
        return data;
    }

    private Map<String, Object> employerOverview(Long employerId) {
        List<Job> jobs = jobRepository.findByEmployerId(employerId);
        long totalApplications = jobs.stream().mapToLong(job -> applicationRepository.countByJobId(job.getId())).sum();
        long hired = jobs.stream().mapToLong(job -> applicationRepository.countByJobIdAndStatus(job.getId(), "HIRED")).sum();
        long shortlisted = jobs.stream().mapToLong(job -> applicationRepository.countByJobIdAndStatus(job.getId(), "SHORTLISTED")).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("myJobs", jobs.size());
        data.put("myApplications", totalApplications);
        data.put("hired", hired);
        data.put("shortlisted", shortlisted);
        data.put("conversionRate", totalApplications == 0 ? 0 : Math.round((double) hired * 100 / totalApplications));
        return data;
    }
}
