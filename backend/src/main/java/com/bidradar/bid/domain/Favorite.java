package com.bidradar.bid.domain;

import com.bidradar.auth.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "favorites",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorites_user_bid",
                columnNames = {"user_id", "bid_notice_id"})
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorites_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_notice_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorites_bid_notice"))
    private BidNotice bidNotice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Favorite create(User user, BidNotice bidNotice) {
        Favorite favorite = new Favorite();
        favorite.user = user;
        favorite.bidNotice = bidNotice;
        return favorite;
    }
}
