package com.jobportal.controller;

import com.jobportal.dto.InterviewFeedbackRequest;
import com.jobportal.dto.ScheduleInterviewRequest;
import com.jobportal.security.SecurityUtil;
import com.jobportal.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/interviews", "/api/v1/interviews"})
@CrossOrigin(origins = "http://localhost:3000")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object schedule(@Valid @RequestBody ScheduleInterviewRequest request) {
        var current = SecurityUtil.currentUser();
        return interviewService.schedule(current.id(), request);
    }

    @PutMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object feedback(@PathVariable Long id, @Valid @RequestBody InterviewFeedbackRequest request) {
        var current = SecurityUtil.currentUser();
        return interviewService.addFeedback(id, current.id(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Object myAsCandidate() {
        var current = SecurityUtil.currentUser();
        return interviewService.myCandidateInterviews(current.id());
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object myAsInterviewer() {
        var current = SecurityUtil.currentUser();
        return interviewService.myInterviewerInterviews(current.id());
    }
}
