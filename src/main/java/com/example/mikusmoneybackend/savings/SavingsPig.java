package com.example.mikusmoneybackend.savings;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.example.mikusmoneybackend.miku.Miku;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_pigs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsPig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saved_money", nullable = false, precision = 19, scale = 2)
    private BigDecimal savedMoney;

    @Column(name = "goal", nullable = false, precision = 19, scale = 2)
    private BigDecimal goal;

    @Column(name = "goal_name", nullable = false, length = 100)
    private String goalName;

    @Column(nullable = false)
    private Boolean broken;

    @Column(name = "broken_at")
    private LocalDateTime brokenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // One-to-one relationship with Miku
    @OneToOne
    @JoinColumn(name = "miku_id", nullable = false)
    private Miku miku;

    // Lifecycle callback - executed before persist
    @PrePersist
    protected void onCreate() {
        if (this.savedMoney == null) {
            this.savedMoney = BigDecimal.ZERO;
        }
        if (this.broken == null) {
            this.broken = false;
        }
    }

    // Helper methods
    public void addMoney(BigDecimal amount) {
        validateNotBroken();
        validateAmount(amount);
        this.savedMoney = this.savedMoney.add(amount);
    }

    public BigDecimal breakPig() {
        validateNotBroken();
        this.broken = true;
        this.brokenAt = LocalDateTime.now();
        BigDecimal savedAmount = this.savedMoney;
        this.savedMoney = BigDecimal.ZERO;
        return savedAmount;
    }

    public boolean canAddMoney() {
        return !this.broken;
    }

    public boolean hasEnoughSavings(BigDecimal amount) {
        return this.savedMoney.compareTo(amount) >= 0;
    }

    private void validateNotBroken() {
        if (this.broken) {
            throw new IllegalStateException("Cannot operate on a broken savings pig");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
