package com.glamik.webpconverter.model;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversionTaskStatus status;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String filesystemName;

    private String convertedName;

    @Column(nullable = false)
    private LocalDateTime taskCreationDate;

    private LocalDateTime taskProcessingDate;

    @Enumerated(EnumType.STRING)
    private ErrorMessage errorMessage;
}
