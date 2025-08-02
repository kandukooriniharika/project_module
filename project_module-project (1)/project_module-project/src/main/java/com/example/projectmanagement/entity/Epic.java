package com.example.projectmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private EpicStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Builder.Default
    private Integer progressPercentage=0;

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    
    // Enums
    public enum EpicStatus {
        OPEN,
        IN_PROGRESS,
        COMPLETED,
        ON_HOLD
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
