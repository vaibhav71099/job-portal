package com.jobportal.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String emailVerificationToken;

    private Long emailVerificationTokenExpiry;

    private String passwordResetToken;

    private Long passwordResetTokenExpiry;

    private String refreshToken;

    private Long refreshTokenExpiry;

    @Column(length = 1000)
    private String skills;

    private Integer experienceYears;

    @Column(length = 500)
    private String education;

    @Column(length = 500)
    private String portfolioUrl;
}
