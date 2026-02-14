package com.jobportal.controller;

import com.jobportal.security.SecurityUtil;
import com.jobportal.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/analytics", "/api/v1/analytics"})
@CrossOrigin(origins = "http://localhost:3000")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public Object overview() {
        var currentUser = SecurityUtil.currentUser();
        return analyticsService.getOverview(currentUser.id(), currentUser.role());
    }
}
