package com.bidradar.auth.domain;

import com.bidradar.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public static User create(String email, String passwordHash, String name) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHash;
        user.name = name;
        user.role = UserRole.USER;
        user.isActive = true;
        return user;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("이미 비활성화된 사용자입니다.");
        }
        this.isActive = false;
    }
}
