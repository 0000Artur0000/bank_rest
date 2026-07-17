package com.example.bankcards.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CardTest {
    @Test
    void preservesMoneyAndExpiryInvariants() {
        User owner = new User("user", "hash", "User", Role.USER, true);
        Card card = new Card("encrypted", "a".repeat(64), "1234", owner, LocalDate.now().plusDays(1), new BigDecimal("10.00"));

        card.debit(new BigDecimal("3.50"));
        card.credit(new BigDecimal("1.25"));

        assertEquals(new BigDecimal("7.75"), card.getBalance());
        assertThrows(IllegalStateException.class, () -> card.debit(new BigDecimal("8.00")));

        Card expired = new Card("encrypted", "b".repeat(64), "5678", owner, LocalDate.now().minusDays(1), BigDecimal.ZERO);
        assertEquals(CardStatus.EXPIRED, expired.effectiveStatus());
        assertThrows(IllegalStateException.class, expired::activate);
    }
}
