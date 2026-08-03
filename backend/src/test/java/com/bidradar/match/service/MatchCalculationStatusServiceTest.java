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
    @DisplayName("재시도 락 재획득에 성공하면(acquireRetryLock이 1을 반환) 재계산 이벤트가 발행된다")
    void retry_락재획득에_성공하면_이벤트가_발행된다() {
        // given: 상태 자체(FAILED/낡은 IN_PROGRESS)가 재시도 가능한지의 판정은 acquireRetryLock의 원자적 CAS
        // 쿼리(MatchCalculationStatusRepositoryTest에서 검증)가 전담하므로, 서비스 단위 테스트에서는
        // 그 결과(1/0)에 따라 서비스가 올바르게 분기하는지만 검증한다.
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "old-token");
        status.markFailed();
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));
        given(matchCalculationStatusRepository.acquireRetryLock(any(), any(), any())).willReturn(1);

        // when
        newService().retry(1L);

        // then
        verify(matchCalculationStatusRepository).acquireRetryLock(any(), any(), any());
        verify(eventPublisher).publishEvent(any(MatchRecalculationRequestedEvent.class));
    }

    @Test
    @DisplayName("재시도 락 재획득에 실패하면(acquireRetryLock이 0을 반환) 전용 에러 코드로 거부되고 이벤트가 발행되지 않는다")
    void retry_락재획득에_실패하면_거부된다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        MatchCalculationStatus status = MatchCalculationStatus.start(company, "token");
        given(matchCalculationStatusRepository.findByCompanyId(company.getId())).willReturn(Optional.of(status));
        given(matchCalculationStatusRepository.acquireRetryLock(any(), any(), any())).willReturn(0);

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
