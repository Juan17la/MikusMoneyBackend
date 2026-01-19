package com.example.mikusmoneybackend.credentials;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.example.mikusmoneybackend.miku.Miku;

import java.time.LocalDateTime;

@Entity
@Table(name = "credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(nullable = false, length = 60)
    private String password; // Should be hashed (bcrypt hash is 60 characters)

    @Column(name = "pin_code", nullable = false, length = 60)
    private String pinCode; // Should be hashed (bcrypt hash is 60 characters)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // One-to-one relationship with Miku
    @OneToOne
    @JoinColumn(name = "miku_id", nullable = false, unique = true)
    private Miku miku;

    // Business methods
    public void updatePassword(String newHashedPassword) {
        if (newHashedPassword == null || newHashedPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        this.password = newHashedPassword;
    }

    public void updatePinCode(String newHashedPinCode) {
        if (newHashedPinCode == null || newHashedPinCode.isBlank()) {
            throw new IllegalArgumentException("PIN code cannot be null or empty");
        }
        this.pinCode = newHashedPinCode;
    }

    public void updateEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.email = newEmail;
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        if (newPhoneNumber == null || newPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        this.phoneNumber = newPhoneNumber;
    }
}
