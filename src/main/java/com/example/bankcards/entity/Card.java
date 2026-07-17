package com.example.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_number", nullable = false, columnDefinition = "text")
    private String encryptedNumber;

    @Column(name = "number_fingerprint", nullable = false, unique = true, length = 64)
    private String numberFingerprint;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CardStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected Card() {
    }

    public Card(
            String encryptedNumber,
            String numberFingerprint,
            String lastFour,
            User owner,
            LocalDate expiryDate,
            BigDecimal balance) {
        this.encryptedNumber = encryptedNumber;
        this.numberFingerprint = numberFingerprint;
        this.lastFour = lastFour;
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.status = CardStatus.ACTIVE;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String encryptedNumber() {
        return encryptedNumber;
    }

    public String numberFingerprint() {
        return numberFingerprint;
    }

    public String getLastFour() {
        return lastFour;
    }

    public User getOwner() {
        return owner;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void block() {
        status = CardStatus.BLOCKED;
    }

    public void activate() {
        if (effectiveStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Expired card cannot be activated");
        }
        status = CardStatus.ACTIVE;
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public CardStatus effectiveStatus() {
        return expiryDate.isBefore(LocalDate.now()) ? CardStatus.EXPIRED : status;
    }

    private static void requirePositive(BigDecimal amount) {
        if (Objects.requireNonNull(amount).signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
