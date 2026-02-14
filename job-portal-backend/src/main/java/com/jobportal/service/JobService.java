package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.exception.ApiException;
import com.jobportal.model.Job;
import com.jobportal.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job addJob(JobRequest request, Long employerId) {
        Job job = new Job();
        applyRequest(job, request, employerId);
        return jobRepository.save(job);
    }

    public Page<Job> getJobs(String keyword, String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Job> specification = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String safeKeyword = keyword.toLowerCase();
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + safeKeyword + "%"),
                    cb.like(cb.lower(root.get("company")), "%" + safeKeyword + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + safeKeyword + "%")
            ));
        }

        if (location != null && !location.isBlank()) {
            String safeLocation = location.toLowerCase();
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")), "%" + safeLocation + "%"));
        }

        return jobRepository.findAll(specification, pageable);
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ApiException("Job not found"));
    }

    public Page<Job> getEmployerJobs(Long employerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jobRepository.findAll((root, query, cb) -> cb.equal(root.get("employerId"), employerId), pageable);
    }

    public Job updateJob(Long id, JobRequest request, Long requesterId, String requesterRole) {
        Job existing = getJobById(id);
        boolean allowed = "ADMIN".equals(requesterRole) || existing.getEmployerId().equals(requesterId);
        if (!allowed) {
            throw new ApiException("Not allowed to edit this job");
        }

        applyRequest(existing, request, existing.getEmployerId());
        return jobRepository.save(existing);
    }

    public void deleteJob(Long id, Long requesterId, String requesterRole) {
        Job existing = getJobById(id);
        boolean allowed = "ADMIN".equals(requesterRole) || existing.getEmployerId().equals(requesterId);
        if (!allowed) {
            throw new ApiException("Not allowed to delete this job");
        }
        jobRepository.delete(existing);
    }

    private void applyRequest(Job job, JobRequest request, Long employerId) {
        job.setTitle(request.title());
        job.setCompany(request.company());
        job.setLocation(request.location());
        job.setSalary(request.salary());
        job.setDescription(request.description());
        job.setEmployerId(employerId);
    }
}
