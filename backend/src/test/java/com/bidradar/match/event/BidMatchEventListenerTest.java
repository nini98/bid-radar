package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.event.BidNoticeCollectedEvent;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.service.MatchCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BidMatchEventListenerTest {

    @Mock
    BidNoticeRepository bidNoticeRepository;
    @Mock
    CompanyRepository companyRepository;
    @Mock
    MatchCalculationService matchCalculationService;

    @InjectMocks
    BidMatchEventListener listener;

    private final BidNotice bid = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final Company companyA = Company.create(User.create("a@bidradar.com", "hash", "회사A"), "회사A");
    private final Company companyB = Company.create(User.create("b@bidradar.com", "hash", "회사B"), "회사B");

    @Test
    @DisplayName("전체 회사에 대해 매칭 계산을 수행한다")
    void 전체_회사에_대해_매칭계산을_수행한다() {
        // given
        given(bidNoticeRepository.findById(1L)).willReturn(Optional.of(bid));
        given(companyRepository.findAll()).willReturn(List.of(companyA, companyB));

        // when
        listener.handle(new BidNoticeCollectedEvent(1L));

        // then
        verify(matchCalculationService).calculateAndSave(bid, companyA);
        verify(matchCalculationService).calculateAndSave(bid, companyB);
    }

    @Test
    @DisplayName("한 회사의 매칭 계산이 실패해도 나머지 회사는 계속 처리된다")
    void 한_회사가_실패해도_나머지는_계속_처리된다() {
        // given
        given(bidNoticeRepository.findById(1L)).willReturn(Optional.of(bid));
        given(companyRepository.findAll()).willReturn(List.of(companyA, companyB));
        doThrow(new RuntimeException("계산 실패")).when(matchCalculationService).calculateAndSave(bid, companyA);

        // when
        listener.handle(new BidNoticeCollectedEvent(1L));

        // then
        verify(matchCalculationService).calculateAndSave(bid, companyB);
    }

    @Test
    @DisplayName("대상 공고를 찾을 수 없으면 매칭 계산을 수행하지 않는다")
    void 공고를_찾을수없으면_매칭계산을_수행하지않는다() {
        // given
        given(bidNoticeRepository.findById(999L)).willReturn(Optional.empty());

        // when
        listener.handle(new BidNoticeCollectedEvent(999L));

        // then
        verify(matchCalculationService, times(0)).calculateAndSave(any(), any());
    }
}
