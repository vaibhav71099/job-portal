package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.exception.ApiException;
import com.jobportal.model.Job;
import com.jobportal.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void getJobByIdThrowsWhenMissing() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> jobService.getJobById(99L));
        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void addJobMapsFieldsCorrectly() {
        JobRequest request = new JobRequest("Backend Dev", "Acme", "Pune", 1200000.0, "Spring Boot role");
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job saved = jobService.addJob(request, 7L);

        assertEquals("Backend Dev", saved.getTitle());
        assertEquals(7L, saved.getEmployerId());
        assertEquals(1200000.0, saved.getSalary());
    }

    @Test
    void getJobsReturnsPageFromRepository() {
        Page<Job> expected = new PageImpl<>(List.of(new Job()));
        when(jobRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(expected);

        Page<Job> result = jobService.getJobs("java", "pune", 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(jobRepository, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
    }
}
