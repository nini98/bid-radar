package com.bidradar.match.service;

import com.bidradar.auth.domain.User;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.dto.response.MatchCalculationStatusResponse;
import com.bidradar.match.event.MatchRecalculationRequestedEvent;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchCalculationStatusServiceTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    MatchCalculationStatusRepository matchCalculationStatusRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    MatchCalculationStatusMapper matchCalculationStatusMapper = new MatchCalculationStatusMapperImpl();

    MatchCalculationStatusService service;

    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");

    private MatchCalculationStatusService newService() {
        return new MatchCalculationStatusService(
                companyRepository, matchCalculationStatusRepository, matchCalculationStatusMapper, eventPublisher);
    }

    @Test
    @DisplayName("계산 이력이 있으면 현재 상태를 그대로 반환한다")
    void getStatus_이력있으면_현재상태를_반환한다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "token");
        status.markDone();
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));

        // when
        MatchCalculationStatusResponse response = newService().getStatus(1L);

        // then
        assertThat(response.status()).isEqualTo(status.getStatus());
    }

    @Test
    @DisplayName("계산 이력이 없으면 status가 null인 응답을 반환한다")
    void getStatus_이력없으면_status_null을_반환한다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.empty());

        // when
        MatchCalculationStatusResponse response = newService().getStatus(1L);

        // then
        assertThat(response.status()).isNull();
    }

    @Test
    @DisplayName("회사 프로필이 없으면 조회 시 404 예외가 발생한다")
    void getStatus_회사없으면_404_예외가_발생한다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> newService().getStatus(1L))
                .isInstanceOf(ApiException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.NOT_FOUND);
    }

    @Test
    @DisplayName("FAILED 상태면 재시도가 허용되고 락 갱신 후 재계산 이벤트가 발행된다")
    void retry_FAILED상태면_락갱신후_이벤트가_발행된다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "old-token");
        status.markFailed();
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));
        given(matchCalculationStatusRepository.acquireLock(any(), any(), any())).willReturn(1);

        // when
        newService().retry(1L);

        // then
        verify(matchCalculationStatusRepository).acquireLock(any(), any(), any());
        verify(eventPublisher).publishEvent(any(MatchRecalculationRequestedEvent.class));
    }

    @Test
    @DisplayName("FAILED가 아닌 상태(IN_PROGRESS/DONE)면 재시도가 거부된다")
    void retry_FAILED가_아니면_거부된다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "token");
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));

        // when // then
        assertThatThrownBy(() -> newService().retry(1L))
                .isInstanceOf(ApiException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.MATCH_CALCULATION_RETRY_NOT_ALLOWED);
        verify(matchCalculationStatusRepository, never()).acquireLock(any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("FAILED 상태여도 락 재획득 시점에 이미 다른 작업이 선점했으면 재시도가 거부된다")
    void retry_락재획득_경쟁에서_밀리면_거부된다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "old-token");
        status.markFailed();
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));
        given(matchCalculationStatusRepository.acquireLock(any(), any(), any())).willReturn(0);

        // when // then
        assertThatThrownBy(() -> newService().retry(1L))
                .isInstanceOf(ApiException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.MATCH_CALCULATION_RETRY_NOT_ALLOWED);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("계산 이력이 없으면(한 번도 저장한 적 없음) 재시도 시 404 예외가 발생한다")
    void retry_이력없으면_404_예외가_발생한다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> newService().retry(1L))
                .isInstanceOf(ApiException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.NOT_FOUND);
    }
}
