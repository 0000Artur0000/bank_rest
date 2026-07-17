package com.example.bankcards.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class RepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void filtersOwnershipStatusAndLocksInIdOrder() {
        User alice = userRepository.save(new User("alice", "hash", "Alice Smith", Role.USER, true));
        User bob = userRepository.save(new User("bob", "hash", "Bob Smith", Role.USER, false));
        LocalDate today = LocalDate.now();
        Card active = cardRepository.save(card("a", "1234", alice, today.plusYears(1)));
        Card blocked = card("b", "5678", alice, today.plusYears(1));
        blocked.block();
        blocked = cardRepository.save(blocked);
        Card expired = cardRepository.save(card("c", "9012", bob, today.minusDays(1)));
        cardRepository.flush();

        assertEquals(List.of(bob), userRepository.findAllFiltered("smith", Role.USER, false, PageRequest.of(0, 10)).getContent());
        assertEquals(List.of(active), cardRepository.findAllFiltered(null, "alice", "1234", CardStatus.ACTIVE, today, PageRequest.of(0, 10)).getContent());
        assertEquals(List.of(expired), cardRepository.findAllFiltered(null, null, null, CardStatus.EXPIRED, today, PageRequest.of(0, 10)).getContent());
        assertTrue(cardRepository.findByIdAndOwnerUsername(active.getId(), "alice").isPresent());
        assertFalse(cardRepository.findByIdAndOwnerUsername(active.getId(), "bob").isPresent());
        assertTrue(cardRepository.existsByOwnerId(alice.getId()));
        assertEquals(List.of(active.getId(), blocked.getId()), cardRepository.findAllByIdForUpdate(List.of(blocked.getId(), active.getId())).stream().map(Card::getId).toList());
    }

    private Card card(String fingerprint, String lastFour, User owner, LocalDate expiryDate) {
        return new Card("encrypted", fingerprint.repeat(64), lastFour, owner, expiryDate, BigDecimal.TEN);
    }
}
