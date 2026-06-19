package com.bidradar.bid.repository;

import com.bidradar.bid.domain.BidStatus;
import com.bidradar.bid.domain.QBidNotice;
import com.bidradar.bid.dto.query.BidSortType;
import com.bidradar.bid.dto.response.BidNoticeSummaryResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class BidNoticeRepositoryImpl implements BidNoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QBidNotice bid = QBidNotice.bidNotice;

    @Override
    public Page<BidNoticeSummaryResponse> search(BidSearchCondition condition, Pageable pageable) {
        BooleanBuilder where = buildWhere(condition);

        List<BidNoticeSummaryResponse> content = queryFactory
                .select(Projections.constructor(BidNoticeSummaryResponse.class,
                        bid.id,
                        bid.title,
                        bid.agency,
                        bid.budget,
                        bid.region,
                        bid.bidType,
                        bid.status,
                        bid.bidDeadline,
                        bid.publishedAt
                ))
                .from(bid)
                .where(where)
                .orderBy(orderBy(condition.sort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(bid.count())
                .from(bid)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countTodayNew() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long count = queryFactory
                .select(bid.count())
                .from(bid)
                .where(bid.publishedAt.goe(todayStart))
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public long countTodayDeadline() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        Long count = queryFactory
                .select(bid.count())
                .from(bid)
                .where(
                        bid.bidDeadline.goe(todayStart),
                        bid.bidDeadline.lt(todayEnd),
                        bid.status.eq(BidStatus.OPEN)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanBuilder buildWhere(BidSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(bid.status.eq(BidStatus.OPEN));

        if (StringUtils.hasText(condition.keyword())) {
            builder.and(bid.title.containsIgnoreCase(condition.keyword())
                    .or(bid.agency.containsIgnoreCase(condition.keyword())));
        }
        if (StringUtils.hasText(condition.region())) {
            builder.and(bid.region.eq(condition.region()));
        }
        if (condition.budgetMin() != null) {
            builder.and(bid.budget.goe(condition.budgetMin()));
        }
        if (condition.budgetMax() != null) {
            builder.and(bid.budget.loe(condition.budgetMax()));
        }
        if (condition.deadlineDays() != null) {
            LocalDateTime deadline = LocalDateTime.now().plusDays(condition.deadlineDays());
            builder.and(bid.bidDeadline.loe(deadline));
        }
        return builder;
    }

    private OrderSpecifier<?> orderBy(BidSortType sort) {
        return switch (sort) {
            case DEADLINE -> bid.bidDeadline.asc();
            case SCORE, LATEST -> bid.publishedAt.desc();
        };
    }
}
