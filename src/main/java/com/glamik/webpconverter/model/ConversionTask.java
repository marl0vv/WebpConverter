package com.glamik.webpconverter.model;

import com.glamik.webpconverter.enums.ConversionTaskStatus;
import com.glamik.webpconverter.enums.ErrorMessage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
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

    @Column
    private String convertedName;

    @Column(nullable = false)
    private LocalDateTime taskCreationDate;

    @Enumerated(EnumType.STRING)
    @Column
    private ErrorMessage errorMessage;
}
