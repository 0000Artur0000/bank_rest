package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByNumberFingerprint(String numberFingerprint);

    boolean existsByOwnerId(Long ownerId);

    Optional<Card> findByIdAndOwnerUsername(Long id, String username);

    @Query("""
            SELECT c
            FROM Card c
            WHERE (:ownerId IS NULL OR c.owner.id = :ownerId)
              AND (:ownerUsername IS NULL OR c.owner.username = :ownerUsername)
              AND (:q IS NULL
                OR c.lastFour LIKE CONCAT('%', :q, '%')
                OR LOWER(c.owner.username) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.owner.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:status IS NULL
                OR (:status = com.example.bankcards.entity.CardStatus.EXPIRED AND c.expiryDate < :today)
                OR (:status <> com.example.bankcards.entity.CardStatus.EXPIRED
                    AND c.expiryDate >= :today
                    AND c.status = :status))
            ORDER BY c.id DESC
            """)
    Page<Card> findAllFiltered(
            @Param("ownerId") Long ownerId,
            @Param("ownerUsername") String ownerUsername,
            @Param("q") String q,
            @Param("status") CardStatus status,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id IN :ids ORDER BY c.id")
    List<Card> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
