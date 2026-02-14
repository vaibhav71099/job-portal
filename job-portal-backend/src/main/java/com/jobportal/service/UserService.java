package com.jobportal.service;

import com.jobportal.dto.LoginRequest;
import com.jobportal.dto.RegisterRequest;
import com.jobportal.dto.UpdateProfileRequest;
import com.jobportal.dto.UserDto;
import com.jobportal.exception.ApiException;
import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationEmailService notificationEmailService;

    @Value("${auth.require-email-verification:false}")
    private boolean requireEmailVerification;

    @Value("${auth.email-verification-expiration-ms:86400000}")
    private long emailVerificationExpiryMs;

    @Value("${auth.password-reset-expiration-ms:1800000}")
    private long passwordResetExpiryMs;

    @Value("${auth.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpiryMs;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationEmailService notificationEmailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationEmailService = notificationEmailService;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ApiException("User already exists with this email");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role().toUpperCase());
        user.setEmailVerified(false);

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(Instant.now().toEpochMilli() + emailVerificationExpiryMs);

        User saved = userRepository.save(user);
        notificationEmailService.sendVerificationEmail(saved.getEmail(), verificationToken);
        return saved;
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ApiException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException("Invalid email or password");
        }

        if (requireEmailVerification && !user.isEmailVerified()) {
            throw new ApiException("Email not verified. Please verify email before login");
        }

        return user;
    }

    public User socialLogin(String provider, String email, String name) {
        String normalizedProvider = provider == null ? "" : provider.trim().toUpperCase();
        if (!"GOOGLE".equals(normalizedProvider) && !"LINKEDIN".equals(normalizedProvider)) {
            throw new ApiException("Unsupported OAuth provider");
        }

        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(name.trim());
                    user.setEmail(email.trim().toLowerCase());
                    user.setPassword(passwordEncoder.encode("oauth-user"));
                    user.setRole("CANDIDATE");
                    user.setEmailVerified(true);
                    return userRepository.save(user);
                });
    }

    public String issueRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().toEpochMilli() + refreshTokenExpiryMs);
        userRepository.save(user);
        return refreshToken;
    }

    public User refreshSession(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ApiException("Invalid refresh token"));

        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry() < Instant.now().toEpochMilli()) {
            throw new ApiException("Refresh token expired");
        }

        return user;
    }

    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ApiException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry() == null || user.getEmailVerificationTokenExpiry() < Instant.now().toEpochMilli()) {
            throw new ApiException("Verification token expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.isEmailVerified()) {
            throw new ApiException("Email already verified");
        }

        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiry(Instant.now().toEpochMilli() + emailVerificationExpiryMs);
        userRepository.save(user);
        notificationEmailService.sendVerificationEmail(user.getEmail(), token);
    }

    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(Instant.now().toEpochMilli() + passwordResetExpiryMs);
            userRepository.save(user);
            notificationEmailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ApiException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry() < Instant.now().toEpochMilli()) {
            throw new ApiException("Reset token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    public UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));
        return toDto(user);
    }

    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setName(request.name().trim());
        user.setSkills(request.skills());
        user.setExperienceYears(request.experienceYears());
        user.setEducation(request.education());
        user.setPortfolioUrl(request.portfolioUrl());
        return toDto(userRepository.save(user));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.getSkills(),
                user.getExperienceYears(),
                user.getEducation(),
                user.getPortfolioUrl()
        );
    }
}
