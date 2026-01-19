package com.example.mikusmoneybackend.account;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.example.mikusmoneybackend.miku.Miku;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_money", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMoney;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // One-to-one relationship with Miku
    @OneToOne
    @JoinColumn(name = "miku_id", nullable = false, unique = true)
    private Miku miku;

    @Version
    @Column(name = "lock_version")
    private long version;

    // Lifecycle callback - executed before persist
    @PrePersist
    protected void onCreate() {
        if (this.totalMoney == null) {
            this.totalMoney = BigDecimal.ZERO;
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Amount must be positive");
    }
}

    public void deposit(BigDecimal amount) {
        validatePositiveAmount(amount);
        this.totalMoney = this.totalMoney.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(amount);
        if (!hasEnoughBalance(amount)) {
            throw new IllegalStateException("Insufficient balance. Current: " + this.totalMoney + ", Required: " + amount);
        }
        this.totalMoney = this.totalMoney.subtract(amount);
    }

    //luego lo cambio, cosa por cosa, cosa por cosa... I wanna cry :(
    public void transfer(Account destinationAccount, BigDecimal amount) {
        if (destinationAccount == null) {
            throw new IllegalArgumentException("Destination account cannot be null");
        }
        this.withdraw(amount);
        destinationAccount.deposit(amount);
    }
    
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.totalMoney.compareTo(amount) >= 0;
    }

    public boolean isEmpty() {
        return this.totalMoney.compareTo(BigDecimal.ZERO) == 0;
    }
}
