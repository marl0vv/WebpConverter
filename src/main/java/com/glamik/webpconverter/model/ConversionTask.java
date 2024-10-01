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
@AllArgsConstructor
@RequiredArgsConstructor
public class ConversionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final ConversionTaskStatus taskStatus;

    @Column(nullable = false)
    private final String fileName;

}
