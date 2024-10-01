package com.glamik.webpconverter.model;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ConversionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversionTaskStatus status;

    @Column(nullable = false)
    private String fileName;
}
