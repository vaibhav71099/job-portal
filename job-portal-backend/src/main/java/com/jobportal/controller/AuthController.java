package com.jobportal.controller;

import com.jobportal.dto.*;
import com.jobportal.security.JwtService;
import com.jobportal.security.SecurityUtil;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = userService.register(request);
        var token = jwtService.generateToken(user);
        var refreshToken = userService.issueRefreshToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, refreshToken, userService.toDto(user)));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var user = userService.login(request);
        var token = jwtService.generateToken(user);
        var refreshToken = userService.issueRefreshToken(user);
        return new AuthResponse(token, refreshToken, userService.toDto(user));
    }

    @PostMapping("/oauth/login")
    public AuthResponse oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        var user = userService.socialLogin(request.provider(), request.email(), request.name());
        var token = jwtService.generateToken(user);
        var refreshToken = userService.issueRefreshToken(user);
        return new AuthResponse(token, refreshToken, userService.toDto(user));
    }

    @GetMapping("/oauth/providers")
    public Map<String, Object> oauthProviders() {
        return Map.of(
                "providers", java.util.List.of("GOOGLE", "LINKEDIN"),
                "mode", "DEV_MOCK"
        );
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        var user = userService.refreshSession(request.refreshToken());
        var token = jwtService.generateToken(user);
        var refreshToken = userService.issueRefreshToken(user);
        return new AuthResponse(token, refreshToken, userService.toDto(user));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> logout() {
        var currentUser = SecurityUtil.currentUser();
        userService.logout(currentUser.id());
        return Map.of("message", "Logged out successfully");
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public UserDto myProfile() {
        var currentUser = SecurityUtil.currentUser();
        return userService.getProfile(currentUser.id());
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public UserDto updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        var currentUser = SecurityUtil.currentUser();
        return userService.updateProfile(currentUser.id(), request);
    }

    @GetMapping("/verify-email")
    public Map<String, String> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return Map.of("message", "Email verified successfully");
    }

    @PostMapping("/resend-verification")
    public Map<String, String> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        userService.resendVerificationEmail(request.email());
        return Map.of("message", "Verification email sent");
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.email());
        return Map.of("message", "If the account exists, reset instructions have been sent");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return Map.of("message", "Password reset successful");
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Object getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
