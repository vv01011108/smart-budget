package com.example.smart_budget.FixedExpense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_expenses")
@Getter
@NoArgsConstructor
public class FixedExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    @Column(name = "billing_day", nullable = false)
    private Integer billingDay;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FixedExpense(String name, BigDecimal amount, Integer billingDay) {
        this.name = name;
        this.amount = amount;
        this.billingDay = billingDay;
    }

    public void update(String name, BigDecimal amount, Integer billingDay) {
        this.name = name;
        this.amount = amount;
        this.billingDay = billingDay;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
