package com.glamik.webpconverter.model;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table()
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ConversionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversionTaskStatus taskStatus;

    @Column(nullable = false)
    private String fileName;

    public ConversionTask(ConversionTaskStatus taskStatus, String fileName) {
        this.taskStatus = taskStatus;
        this.fileName = fileName;
    }
}
