package com.bidradar.match.service;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyBidPreferenceRepository;
import com.bidradar.company.repository.CompanyBusinessAreaRepository;
import com.bidradar.company.repository.CompanyTechTagRepository;
import com.bidradar.match.domain.BidMatchResult;
import com.bidradar.match.domain.BidMatchResultStatus;
import com.bidradar.match.domain.MatchGrade;
import com.bidradar.match.repository.BidMatchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
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
        // markFailed()만 검증하는 테스트는 아래 스텁을 안 쓰므로(buildProfile/matchingEngine을
        // 안 거침) lenient로 선언해 strict stubbing이 "사용 안 된 스텁"으로 오인하지 않게 한다.
        lenient().when(companyTechTagRepository.findByCompanyId(any())).thenReturn(List.of());
        lenient().when(companyBusinessAreaRepository.findByCompanyId(any())).thenReturn(List.of());
        lenient().when(companyBidPreferenceRepository.findByCompanyId(any())).thenReturn(Optional.empty());
        lenient().when(matchingEngine.calculate(any(), any(), any())).thenReturn(calculationResult);
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

    @Test
    @DisplayName("기존 매칭 결과가 있을 때 실패 기록하면 점수를 전부 비우고 FAILED로 남긴다")
    void 기존결과있을때_실패기록하면_점수를_비우고_FAILED로_남긴다() {
        // given
        BidMatchResult existing = BidMatchResult.create(
                bid, company, new BigDecimal("70"), MatchGrade.RECOMMENDED,
                new BigDecimal("20"), new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("25"),
                "[\"백엔드\"]", "이전 성공 사유");
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(any(), any())).willReturn(Optional.of(existing));

        // when
        service.markFailed(bid, company, "RuntimeException: 계산 중 오류");

        // then
        assertThat(existing.getStatus()).isEqualTo(BidMatchResultStatus.FAILED);
        assertThat(existing.getErrorMessage()).isEqualTo("RuntimeException: 계산 중 오류");
        assertThat(existing.getTotalScore()).isNull();
        assertThat(existing.getGrade()).isNull();
        assertThat(existing.getScoreTech()).isNull();
        assertThat(existing.getMatchedKeywords()).isNull();
        verify(bidMatchResultRepository).save(existing);
    }

    @Test
    @DisplayName("기존 매칭 결과가 없을 때 실패 기록하면 FAILED row를 새로 생성한다")
    void 기존결과없을때_실패기록하면_FAILED_row를_새로생성한다() {
        // given
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(any(), any())).willReturn(Optional.empty());

        // when
        service.markFailed(bid, company, "RuntimeException: 계산 중 오류");

        // then
        ArgumentCaptor<BidMatchResult> captor = ArgumentCaptor.forClass(BidMatchResult.class);
        verify(bidMatchResultRepository).save(captor.capture());
        BidMatchResult saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(BidMatchResultStatus.FAILED);
        assertThat(saved.getErrorMessage()).isEqualTo("RuntimeException: 계산 중 오류");
        assertThat(saved.getTotalScore()).isNull();
        assertThat(saved.getGrade()).isNull();
    }

    @Test
    @DisplayName("FAILED였던 결과가 재계산에 성공하면 점수와 SUCCESS로 완전히 갱신되고 실패 메시지가 지워진다")
    void FAILED였던결과가_재계산성공하면_SUCCESS로_완전히_갱신된다() {
        // given
        BidMatchResult existing = BidMatchResult.createFailed(bid, company, "이전 실패 사유");
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(any(), any())).willReturn(Optional.of(existing));

        // when
        service.calculateAndSave(bid, company);

        // then
        assertThat(existing.getStatus()).isEqualTo(BidMatchResultStatus.SUCCESS);
        assertThat(existing.getErrorMessage()).isNull();
        assertThat(existing.getTotalScore()).isEqualByComparingTo("85");
        assertThat(existing.getGrade()).isEqualTo(MatchGrade.STRONG_REVIEW);
        verify(bidMatchResultRepository).save(existing);
    }
}
