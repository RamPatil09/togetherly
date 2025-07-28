package com.socialmedia.togetherly.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Optional: if you're manually constructing instead of using Builder
    public VerificationToken(String token, User user) {
        this.id = java.util.UUID.randomUUID().toString();
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusDays(1);
    }
}

