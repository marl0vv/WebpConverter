package com.glamik.webpconverter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "conversion_tasks")
@Getter @Setter
@RequiredArgsConstructor
public class ConversionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "task_status", nullable = false)
    private String taskStatus;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    public ConversionTask(String taskStatus, String fileName) {
        this.taskStatus = taskStatus;
        this.fileName = fileName;
    }
}
