package com.example.mikusmoneybackend.miku;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MikuResponse {

    private Long id;
    private String name;
    private String lastName;
    private LocalDate birthDate;
    private String publicCode;
    private LocalDateTime createdAt;
    private String email;
    private String phoneNumber;
}
