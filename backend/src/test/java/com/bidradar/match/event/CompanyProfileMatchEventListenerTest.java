package com.bidradar.match.event;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatusType;
import com.bidradar.match.service.MatchCalculationStatusCoordinator;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyProfileMatchEventListenerTest {

    private static final String TOKEN = "test-token";

    @Mock
    CompanyRepository companyRepository;
    @Mock
    BidNoticeRepository bidNoticeRepository;
    @Mock
    MatchCalculationStatusCoordinator matchCalculationStatusCoordinator;
    @Mock
    TaskExecutor matchTaskExecutor;

    @InjectMocks
    CompanyProfileMatchEventListener listener;

    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");
    private final BidNotice bidA = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "공고 A", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final BidNotice bidB = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-2", "G2B", "공고 B", null, null, null, null, null, null, null,
            null, null, null, null, null));

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
    @DisplayName("회사의 전체 공고에 대해 매칭 계산을 수행하고 상태를 DONE으로 기록한다")
    void 전체_공고에_대해_매칭계산을_수행하고_DONE으로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));
        given(matchCalculationStatusCoordinator.calculateAndSaveIfOwner(bidA, company, TOKEN)).willReturn(true);
        given(matchCalculationStatusCoordinator.calculateAndSaveIfOwner(bidB, company, TOKEN)).willReturn(true);

        // when
        listener.handle(new MatchRecalculationRequestedEvent(1L, TOKEN));

        // then
        verify(matchCalculationStatusCoordinator).calculateAndSaveIfOwner(bidA, company, TOKEN);
        verify(matchCalculationStatusCoordinator).calculateAndSaveIfOwner(bidB, company, TOKEN);
        verify(matchCalculationStatusCoordinator).finish(1L, TOKEN, MatchCalculationStatusType.DONE);
    }

    @Test
    @DisplayName("한 공고의 매칭 계산이 실패해도 나머지 공고는 계속 처리되고 최종 상태는 DONE으로 기록된다")
    void 한_공고가_실패해도_나머지는_계속_처리되고_DONE으로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));
        doThrow(new RuntimeException("계산 실패"))
                .when(matchCalculationStatusCoordinator).calculateAndSaveIfOwner(bidA, company, TOKEN);
        given(matchCalculationStatusCoordinator.calculateAndSaveIfOwner(bidB, company, TOKEN)).willReturn(true);

        // when
        listener.handle(new MatchRecalculationRequestedEvent(1L, TOKEN));

        // then
        verify(matchCalculationStatusCoordinator).calculateAndSaveIfOwner(bidB, company, TOKEN);
        verify(matchCalculationStatusCoordinator).finish(1L, TOKEN, MatchCalculationStatusType.DONE);
    }

    @Test
    @DisplayName("대상 회사를 찾을 수 없으면 계산은 수행하지 않고 즉시 FAILED로 기록한다")
    void 회사를_찾을수없으면_계산없이_FAILED로_기록한다() {
        // given
        given(companyRepository.findById(999L)).willReturn(Optional.empty());

        // when
        listener.handle(new MatchRecalculationRequestedEvent(999L, TOKEN));

        // then
        verify(matchCalculationStatusCoordinator, never()).calculateAndSaveIfOwner(any(), any(), any());
        verify(matchCalculationStatusCoordinator).finish(999L, TOKEN, MatchCalculationStatusType.FAILED);
    }

    @Test
    @DisplayName("회사 조회 중 예외가 발생해도(일시적 DB 오류 등) FAILED로 기록한다")
    void 회사조회중_예외가_발생해도_FAILED로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willThrow(new RuntimeException("DB 오류"));

        // when
        listener.handle(new MatchRecalculationRequestedEvent(1L, TOKEN));

        // then
        verify(matchCalculationStatusCoordinator).finish(1L, TOKEN, MatchCalculationStatusType.FAILED);
    }

    @Test
    @DisplayName("루프 밖에서 예외가 발생하면 상태를 FAILED로 기록한다")
    void 루프밖_예외가_발생하면_FAILED로_기록한다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willThrow(new RuntimeException("DB 오류"));

        // when
        listener.handle(new MatchRecalculationRequestedEvent(1L, TOKEN));

        // then
        verify(matchCalculationStatusCoordinator).finish(1L, TOKEN, MatchCalculationStatusType.FAILED);
    }

    @Test
    @DisplayName("스레드풀 포화로 작업 제출이 거부되면 계산은 시작조차 되지 않고 즉시 FAILED로 기록된다")
    void 작업제출이_거부되면_즉시_FAILED로_기록한다() {
        // given
        doThrow(new TaskRejectedException("풀 포화")).when(matchTaskExecutor).execute(any());

        // when
        listener.handle(new MatchRecalculationRequestedEvent(1L, TOKEN));

        // then
        verify(companyRepository, never()).findById(any());
        verify(matchCalculationStatusCoordinator, never()).calculateAndSaveIfOwner(any(), any(), any());
        verify(matchCalculationStatusCoordinator).finish(1L, TOKEN, MatchCalculationStatusType.FAILED);
    }

    @Test
    @DisplayName("다른 작업에 락을 넘겨준 것이 감지되면 즉시 루프를 중단하고 최종 상태를 기록하지 않는다")
    void 락을_넘겨준것이_감지되면_루프를_중단하고_상태를_기록하지않는다() {
        // given
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));
        given(matchCalculationStatusCoordinator.calculateAndSaveIfOwner(bidA, company, TOKEN)).willReturn(false);

        // when
        listener.handle(new MatchRecalculationRequestedEvent(1L, TOKEN));

        // then: bidA에서 이미 밀린 것이 확인됐으므로 bidB는 시도조차 하지 않고, 최종 상태도 기록하지 않는다
        //       (이미 다른 작업이 소유권을 가져갔으니 그 작업이 마무리할 몫이다).
        verify(matchCalculationStatusCoordinator, never()).calculateAndSaveIfOwner(bidB, company, TOKEN);
        verify(matchCalculationStatusCoordinator, never()).finish(any(), any(), any());
    }
}
