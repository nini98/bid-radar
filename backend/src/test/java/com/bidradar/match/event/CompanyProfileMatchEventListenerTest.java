package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.event.CompanyProfileSavedEvent;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import com.bidradar.match.service.MatchCalculationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyProfileMatchEventListenerTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    BidNoticeRepository bidNoticeRepository;
    @Mock
    MatchCalculationService matchCalculationService;
    @Mock
    MatchCalculationStatusRepository matchCalculationStatusRepository;

    @InjectMocks
    CompanyProfileMatchEventListener listener;

    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");
    private final BidNotice bidA = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "공고 A", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final BidNotice bidB = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-2", "G2B", "공고 B", null, null, null, null, null, null, null,
            null, null, null, null, null));

    @Test
    @DisplayName("회사의 전체 공고에 대해 매칭 계산을 수행하고 상태를 DONE으로 기록한다")
    void 전체_공고에_대해_매칭계산을_수행하고_DONE으로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));
        MatchCalculationStatus status = MatchCalculationStatus.start(company);
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));

        // when
        listener.handle(new CompanyProfileSavedEvent(1L));

        // then
        verify(matchCalculationService).calculateAndSave(bidA, company);
        verify(matchCalculationService).calculateAndSave(bidB, company);
        verify(matchCalculationStatusRepository).save(status);
        assertThat(status.getStatus()).isEqualTo(MatchCalculationStatusType.DONE);
    }

    @Test
    @DisplayName("한 공고의 매칭 계산이 실패해도 나머지 공고는 계속 처리되고 최종 상태는 DONE이다")
    void 한_공고가_실패해도_나머지는_계속_처리되고_DONE으로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));
        doThrow(new RuntimeException("계산 실패")).when(matchCalculationService).calculateAndSave(bidA, company);
        MatchCalculationStatus status = MatchCalculationStatus.start(company);
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));

        // when
        listener.handle(new CompanyProfileSavedEvent(1L));

        // then
        verify(matchCalculationService).calculateAndSave(bidB, company);
        assertThat(status.getStatus()).isEqualTo(MatchCalculationStatusType.DONE);
    }

    @Test
    @DisplayName("대상 회사를 찾을 수 없으면 매칭 계산도, 상태 기록도 수행하지 않는다")
    void 회사를_찾을수없으면_계산과_상태기록을_수행하지않는다() {
        // given
        given(companyRepository.findById(999L)).willReturn(Optional.empty());

        // when
        listener.handle(new CompanyProfileSavedEvent(999L));

        // then
        verify(matchCalculationService, times(0)).calculateAndSave(any(), any());
        verify(matchCalculationStatusRepository, never()).findByCompanyId(any());
    }

    @Test
    @DisplayName("루프 밖에서 예외가 발생하면 상태를 FAILED로 기록한다")
    void 루프밖_예외가_발생하면_FAILED로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willThrow(new RuntimeException("DB 오류"));
        MatchCalculationStatus status = MatchCalculationStatus.start(company);
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));

        // when
        listener.handle(new CompanyProfileSavedEvent(1L));

        // then
        assertThat(status.getStatus()).isEqualTo(MatchCalculationStatusType.FAILED);
    }
}
