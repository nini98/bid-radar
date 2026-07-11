package com.bidradar.match.service;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyBidPreferenceRepository;
import com.bidradar.company.repository.CompanyBusinessAreaRepository;
import com.bidradar.company.repository.CompanyTechTagRepository;
import com.bidradar.match.domain.BidMatchResult;
import com.bidradar.match.domain.MatchGrade;
import com.bidradar.match.repository.BidMatchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchCalculationServiceTest {

    @Mock
    CompanyTechTagRepository companyTechTagRepository;
    @Mock
    CompanyBusinessAreaRepository companyBusinessAreaRepository;
    @Mock
    CompanyBidPreferenceRepository companyBidPreferenceRepository;
    @Mock
    BidMatchResultRepository bidMatchResultRepository;
    @Mock
    MatchingEngine matchingEngine;

    MatchCalculationService service;

    private final BidNotice bid = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");

    private final MatchCalculationResult calculationResult = new MatchCalculationResult(
            new BigDecimal("85"), MatchGrade.STRONG_REVIEW,
            new BigDecimal("20"), new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("20"),
            "[]", "테스트 사유"
    );

    @BeforeEach
    void setUp() {
        service = new MatchCalculationService(
                companyTechTagRepository, companyBusinessAreaRepository, companyBidPreferenceRepository,
                bidMatchResultRepository, matchingEngine
        );
        given(companyTechTagRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBusinessAreaRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBidPreferenceRepository.findByCompanyId(any())).willReturn(Optional.empty());
        given(matchingEngine.calculate(any(), any(), any())).willReturn(calculationResult);
    }

    @Test
    @DisplayName("기존 매칭 결과가 없으면 새로 저장한다")
    void 기존결과없으면_신규저장() {
        // given
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(any(), any())).willReturn(Optional.empty());

        // when
        service.calculateAndSave(bid, company);

        // then
        verify(bidMatchResultRepository).save(any(BidMatchResult.class));
    }

    @Test
    @DisplayName("기존 매칭 결과가 있으면 갱신한다")
    void 기존결과있으면_갱신() {
        // given
        BidMatchResult existing = BidMatchResult.create(
                bid, company, new BigDecimal("40"), MatchGrade.NEED_REVIEW,
                null, null, null, null, null, "이전 사유");
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(any(), any())).willReturn(Optional.of(existing));

        // when
        service.calculateAndSave(bid, company);

        // then
        assertThat(existing.getTotalScore()).isEqualByComparingTo("85");
        assertThat(existing.getGrade()).isEqualTo(MatchGrade.STRONG_REVIEW);
        assertThat(existing.getScoreReason()).isEqualTo("테스트 사유");
        verify(bidMatchResultRepository).save(existing);
    }
}
