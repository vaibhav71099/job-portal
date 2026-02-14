package com.jobportal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private Long candidateId;

    @Column(length = 2048)
    private String resume;

    private String status;

    private Integer atsScore;

    @Column(nullable = false, updatable = false)
    private Instant appliedAt;

    @PrePersist
    public void prePersist() {
        this.appliedAt = Instant.now();
    }
}
