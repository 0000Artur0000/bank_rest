package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u
            FROM User u
            WHERE (:q IS NULL
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:enabled IS NULL OR u.enabled = :enabled)
            ORDER BY u.id DESC
            """)
    Page<User> findAllFiltered(
            @Param("q") String q,
            @Param("role") Role role,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}
