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
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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
    @Mock
    TaskExecutor matchTaskExecutor;

    @InjectMocks
    BidMatchEventListener listener;

    private final BidNotice bid = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final Company companyA = Company.create(User.create("a@bidradar.com", "hash", "회사A"), "회사A");
    private final Company companyB = Company.create(User.create("b@bidradar.com", "hash", "회사B"), "회사B");

    @BeforeEach
    void setUp() {
        // matchTaskExecutor는 @Async 대신 리스너가 직접 호출하는 대상이라, 테스트에서는
        // "제출된 작업을 즉시 동기 실행"하도록 스텁해야 기존처럼 결정론적으로 검증할 수 있다.
        // lenient(): 제출 거부를 검증하는 테스트에서는 이 기본 스텁이 재정의되어 쓰이지 않으므로,
        // strict stubbing이 "사용되지 않은 스텁"으로 오인하지 않게 한다.
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(matchTaskExecutor).execute(any());
    }

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

    @Test
    @DisplayName("작업 제출이 거부되면 계산은 시작조차 되지 않고 전체 회사가 즉시 FAILED로 기록된다")
    void 작업제출이_거부되면_전체_회사가_FAILED로_기록된다() {
        // given
        doThrow(new TaskRejectedException("풀 포화")).when(matchTaskExecutor).execute(any());
        given(bidNoticeRepository.findById(1L)).willReturn(Optional.of(bid));
        given(companyRepository.findAll()).willReturn(List.of(companyA, companyB));

        // when
        listener.handle(new BidNoticeCollectedEvent(1L));

        // then
        verify(matchCalculationService, never()).calculateAndSave(any(), any());
        verify(matchCalculationService).markFailed(eq(bid), eq(companyA), any());
        verify(matchCalculationService).markFailed(eq(bid), eq(companyB), any());
    }

    @Test
    @DisplayName("제출이 거부됐을 때 공고를 찾을 수 없으면 실패 기록을 시도하지 않는다")
    void 제출거부시_공고를_찾을수없으면_실패기록을_시도하지않는다() {
        // given
        doThrow(new TaskRejectedException("풀 포화")).when(matchTaskExecutor).execute(any());
        given(bidNoticeRepository.findById(999L)).willReturn(Optional.empty());

        // when
        listener.handle(new BidNoticeCollectedEvent(999L));

        // then
        verify(companyRepository, never()).findAll();
        verify(matchCalculationService, never()).markFailed(any(), any(), any());
    }

    @Test
    @DisplayName("제출 거부 처리 중 한 회사의 실패 기록 저장이 또 실패해도 나머지 회사는 계속 처리된다")
    void 제출거부처리중_한회사의_실패기록저장이_또실패해도_나머지회사는_계속처리된다() {
        // given
        doThrow(new TaskRejectedException("풀 포화")).when(matchTaskExecutor).execute(any());
        given(bidNoticeRepository.findById(1L)).willReturn(Optional.of(bid));
        given(companyRepository.findAll()).willReturn(List.of(companyA, companyB));
        doThrow(new RuntimeException("저장 실패")).when(matchCalculationService)
                .markFailed(eq(bid), eq(companyA), any());

        // when
        listener.handle(new BidNoticeCollectedEvent(1L));

        // then
        verify(matchCalculationService).markFailed(eq(bid), eq(companyB), any());
    }
}
