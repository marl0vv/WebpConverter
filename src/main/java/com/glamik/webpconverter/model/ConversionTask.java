package com.glamik.webpconverter.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "conversion_tasks")
@Getter @Setter
public class ConversionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "task_status", nullable = false)
    private String taskStatus;

    @Column(name = "file_name", nullable = false)
    private String fileName;
}
