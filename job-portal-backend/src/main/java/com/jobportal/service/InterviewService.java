package com.jobportal.service;

import com.jobportal.dto.InterviewFeedbackRequest;
import com.jobportal.dto.ScheduleInterviewRequest;
import com.jobportal.exception.ApiException;
import com.jobportal.model.Interview;
import com.jobportal.repository.InterviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final NotificationService notificationService;

    public InterviewService(InterviewRepository interviewRepository, NotificationService notificationService) {
        this.interviewRepository = interviewRepository;
        this.notificationService = notificationService;
    }

    public Interview schedule(Long interviewerId, ScheduleInterviewRequest request) {
        Interview interview = new Interview();
        interview.setApplicationId(request.applicationId());
        interview.setCandidateId(request.candidateId());
        interview.setInterviewerId(interviewerId);
        interview.setScheduledAtEpochMs(request.scheduledAtEpochMs());
        interview.setStatus("SCHEDULED");

        Interview saved = interviewRepository.save(interview);
        notificationService.create(
                request.candidateId(),
                "INTERVIEW",
                "Interview scheduled for application #" + request.applicationId()
        );
        return saved;
    }

    public Interview addFeedback(Long interviewId, Long interviewerId, InterviewFeedbackRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApiException("Interview not found"));

        if (!interview.getInterviewerId().equals(interviewerId)) {
            throw new ApiException("Not allowed to update this interview");
        }

        interview.setFeedback(request.feedback());
        interview.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        Interview saved = interviewRepository.save(interview);

        notificationService.create(
                interview.getCandidateId(),
                "INTERVIEW_FEEDBACK",
                "Interview #" + interviewId + " updated with status " + saved.getStatus()
        );
        return saved;
    }

    public List<Interview> myCandidateInterviews(Long candidateId) {
        return interviewRepository.findByCandidateIdOrderByScheduledAtEpochMsDesc(candidateId);
    }

    public List<Interview> myInterviewerInterviews(Long interviewerId) {
        return interviewRepository.findByInterviewerIdOrderByScheduledAtEpochMsDesc(interviewerId);
    }
}
