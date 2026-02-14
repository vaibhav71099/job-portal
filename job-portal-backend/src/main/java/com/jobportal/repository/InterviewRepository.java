package com.jobportal.repository;

import com.jobportal.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByCandidateIdOrderByScheduledAtEpochMsDesc(Long candidateId);
    List<Interview> findByInterviewerIdOrderByScheduledAtEpochMsDesc(Long interviewerId);
}
