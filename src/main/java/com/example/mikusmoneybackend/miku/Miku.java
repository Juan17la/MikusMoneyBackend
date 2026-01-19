package com.example.mikusmoneybackend.miku;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.example.mikusmoneybackend.account.Account;
import com.example.mikusmoneybackend.credentials.Credential;
import com.example.mikusmoneybackend.savings.SavingsPig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mikus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Miku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "public_code", nullable = false, unique = true, length = 20)
    private String publicCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // One-to-one relationship with Credential
    @OneToOne(mappedBy = "miku", cascade = CascadeType.ALL, orphanRemoval = true)
    private Credential credential;

    // One-to-one relationship with Account
    @OneToOne(mappedBy = "miku", cascade = CascadeType.ALL, orphanRemoval = true)
    private Account account;

    // One-to-many relationship with SavingsPig
    @OneToMany(mappedBy = "miku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingsPig> savingsPigs;

    // Lifecycle callback - executed before persist
    @PrePersist
    protected void onCreate() {
        if (this.publicCode == null) {
            this.publicCode = generatePublicCode();
        }
    }

    // Helper method to get full name
    public String getFullName() {
        return name + " " + lastName;
    }

    // Generate a unique public code (this is a basic implementation, should be enhanced in service)
    public String generatePublicCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        // Take the last 10 characters to ensure we always have enough length
        return timestamp.substring(Math.max(0, timestamp.length() - 10));
    }

    // Business method to check if Miku is adult (18+)
    public boolean isAdult() {
        return birthDate != null && birthDate.isBefore(LocalDate.now().minusYears(18));
    }

    // Business method to get age
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
}
