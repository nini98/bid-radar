package com.bidradar.match.service;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchCalculationStatusCoordinatorTest {

    @Mock
    MatchCalculationStatusRepository matchCalculationStatusRepository;
    @Mock
    MatchCalculationService matchCalculationService;

    @InjectMocks
    MatchCalculationStatusCoordinator coordinator;

    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");
    private final BidNotice bid = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "공고 A", null, null, null, null, null, null, null,
            null, null, null, null, null));

    @Test
    @DisplayName("토큰이 유효하면 결과를 저장하고 true를 반환한다")
    void 토큰이_유효하면_저장하고_true를_반환한다() {
        // given
        given(matchCalculationStatusRepository.heartbeat(company.getId(), "token")).willReturn(1);

        // when
        boolean result = coordinator.calculateAndSaveIfOwner(bid, company, "token");

        // then
        assertThat(result).isTrue();
        verify(matchCalculationService).calculateAndSave(bid, company);
    }

    @Test
    @DisplayName("토큰이 이미 밀렸으면 결과를 저장하지 않고 false를 반환한다")
    void 토큰이_밀렸으면_저장하지않고_false를_반환한다() {
        // given
        given(matchCalculationStatusRepository.heartbeat(company.getId(), "old-token")).willReturn(0);

        // when
        boolean result = coordinator.calculateAndSaveIfOwner(bid, company, "old-token");

        // then
        assertThat(result).isFalse();
        verify(matchCalculationService, never()).calculateAndSave(any(), any());
    }

    @Test
    @DisplayName("토큰이 일치하면 finish가 상태를 반영한다")
    void 토큰이_일치하면_finish가_반영된다() {
        // given
        given(matchCalculationStatusRepository.finish(1L, "token", MatchCalculationStatusType.DONE)).willReturn(1);

        // when
        coordinator.finish(1L, "token", MatchCalculationStatusType.DONE);

        // then
        verify(matchCalculationStatusRepository).finish(1L, "token", MatchCalculationStatusType.DONE);
    }
}
