package com.bidradar.company.service;

import com.bidradar.auth.domain.User;
import com.bidradar.auth.repository.UserRepository;
import com.bidradar.code.domain.BusinessArea;
import com.bidradar.code.domain.TechTag;
import com.bidradar.code.repository.BusinessAreaRepository;
import com.bidradar.code.repository.TechTagRepository;
import com.bidradar.code.service.CodeMapper;
import com.bidradar.code.service.CodeMapperImpl;
import com.bidradar.common.exception.ApiException;
import com.bidradar.common.response.ResultCode;
import com.bidradar.company.domain.Company;
import com.bidradar.company.domain.CompanyBidPreference;
import com.bidradar.company.domain.CompanyCertificate;
import com.bidradar.company.domain.CompanyTechTag;
import com.bidradar.company.dto.request.CompanyProfileRequest;
import com.bidradar.company.dto.request.CompanyProfileRequest.BidPreferenceRequest;
import com.bidradar.company.dto.request.CompanyProfileRequest.ProjectExperienceRequest;
import com.bidradar.company.dto.response.CompanyProfileResponse;
import com.bidradar.company.repository.CompanyBidPreferenceRepository;
import com.bidradar.company.repository.CompanyBusinessAreaRepository;
import com.bidradar.company.repository.CompanyCertificateRepository;
import com.bidradar.company.repository.CompanyProjectExperienceRepository;
import com.bidradar.company.event.CompanyProfileSavedEvent;
import com.bidradar.company.repository.CompanyRepository;
import com.bidradar.company.repository.CompanyTechTagRepository;
import com.bidradar.match.domain.MatchCalculationStatus;
import com.bidradar.match.repository.MatchCalculationStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyProfileServiceTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    CompanyTechTagRepository companyTechTagRepository;
    @Mock
    CompanyBusinessAreaRepository companyBusinessAreaRepository;
    @Mock
    CompanyCertificateRepository companyCertificateRepository;
    @Mock
    CompanyProjectExperienceRepository companyProjectExperienceRepository;
    @Mock
    CompanyBidPreferenceRepository companyBidPreferenceRepository;
    @Mock
    TechTagRepository techTagRepository;
    @Mock
    BusinessAreaRepository businessAreaRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    MatchCalculationStatusRepository matchCalculationStatusRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    final CodeMapper codeMapper = new CodeMapperImpl();
    final CompanyProfileMapper companyProfileMapper = new CompanyProfileMapperImpl();

    CompanyProfileService companyProfileService;

    @BeforeEach
    void setUp() {
        companyProfileService = new CompanyProfileService(
                companyRepository, companyTechTagRepository, companyBusinessAreaRepository,
                companyCertificateRepository, companyProjectExperienceRepository, companyBidPreferenceRepository,
                techTagRepository, businessAreaRepository, userRepository, codeMapper, companyProfileMapper,
                matchCalculationStatusRepository, eventPublisher
        );
    }

    private CompanyProfileRequest emptyRequest(String companyName) {
        return new CompanyProfileRequest(
                companyName, null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null,
                null, null, null
        );
    }

    @Test
    @DisplayName("회사 프로필이 없는 사용자가 조회하면 null을 반환한다")
    void getProfile_회사없으면_null반환() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when
        CompanyProfileResponse result = companyProfileService.getProfile(1L);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("회사 프로필이 있는 사용자가 조회하면 하위 데이터를 포함해 조립된 응답을 반환한다")
    void getProfile_회사있으면_하위데이터포함해서_반환() {
        // given
        User user = User.create("owner@bidradar.com", "hash", "홍길동");
        Company company = Company.create(user, "델타소프트");
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        given(companyTechTagRepository.findByCompanyId(any()))
                .willReturn(List.of(CompanyTechTag.create(company, TechTag.create("Spring"))));
        given(companyBusinessAreaRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyCertificateRepository.findByCompanyId(any()))
                .willReturn(List.of(CompanyCertificate.create(company, "정보처리기사")));
        given(companyProjectExperienceRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBidPreferenceRepository.findByCompanyId(any())).willReturn(Optional.empty());

        // when
        CompanyProfileResponse result = companyProfileService.getProfile(1L);

        // then
        assertThat(result.companyName()).isEqualTo("델타소프트");
        assertThat(result.techTags()).extracting("name").containsExactly("Spring");
        assertThat(result.certificates()).containsExactly("정보처리기사");
        assertThat(result.bidPreference()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 techTagId가 포함되면 VALIDATION_ERROR 예외가 발생한다")
    void saveProfile_존재하지않는_techTagId_예외() {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(1L, 2L), List.of(), List.of(), List.of(), null,
                null, null, null
        );
        given(techTagRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(TechTag.create("Spring")));

        // when // then
        assertThatThrownBy(() -> companyProfileService.saveProfile(1L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getResultCode()).isEqualTo(ResultCode.VALIDATION_ERROR));
    }

    @Test
    @DisplayName("존재하지 않는 businessAreaId가 포함되면 VALIDATION_ERROR 예외가 발생한다")
    void saveProfile_존재하지않는_businessAreaId_예외() {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(1L, 2L), List.of(), List.of(), null,
                null, null, null
        );
        given(businessAreaRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(BusinessArea.create("SI")));

        // when // then
        assertThatThrownBy(() -> companyProfileService.saveProfile(1L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getResultCode()).isEqualTo(ResultCode.VALIDATION_ERROR));
    }

    @Test
    @DisplayName("budgetMin이 budgetMax보다 크면 VALIDATION_ERROR 예외가 발생한다")
    void saveProfile_budgetMin이_budgetMax보다크면_예외() {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", null, null, null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(),
                new BidPreferenceRequest(List.of(), 100_000_000L, 1_000_000L, null, List.of(), List.of()),
                null, null, null
        );

        // when // then
        assertThatThrownBy(() -> companyProfileService.saveProfile(1L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getResultCode()).isEqualTo(ResultCode.VALIDATION_ERROR));
    }

    @Test
    @DisplayName("회사가 없는 사용자가 저장하면 신규 회사가 생성되고 하위 데이터가 저장된다")
    void saveProfile_회사없는사용자가_저장시_신규회사생성() {
        // given
        CompanyProfileRequest request = new CompanyProfileRequest(
                "델타소프트", "123-45-67890", "SI", 2015, "중소기업", "서울", "서울시 강남구", "https://delta.co.kr", "강점",
                List.of(), List.of(), List.of("정보처리기사"),
                List.of(new ProjectExperienceRequest("SI", "설명")),
                new BidPreferenceRequest(List.of("서울"), 1_000_000L, 100_000_000L, 7, List.of("일반경쟁"), List.of("단독수행")),
                "홍길동", "hong@delta.co.kr", "010-1234-5678"
        );
        User user = User.create("owner@bidradar.com", "hash", "홍길동");
        given(companyRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userRepository.getReferenceById(1L)).willReturn(user);
        given(companyRepository.save(any(Company.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(companyTechTagRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBusinessAreaRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyCertificateRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyProjectExperienceRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBidPreferenceRepository.findByCompanyId(any())).willReturn(Optional.empty());
        given(matchCalculationStatusRepository.findByCompanyId(any())).willReturn(Optional.empty());

        // when
        CompanyProfileResponse response = companyProfileService.saveProfile(1L, request);

        // then
        verify(companyRepository).save(any(Company.class));
        verify(companyCertificateRepository).saveAll(anyList());
        verify(companyProjectExperienceRepository).saveAll(anyList());
        verify(companyBidPreferenceRepository).save(any(CompanyBidPreference.class));
        verify(matchCalculationStatusRepository).save(any(MatchCalculationStatus.class));
        verify(eventPublisher).publishEvent(any(CompanyProfileSavedEvent.class));
        assertThat(response.companyName()).isEqualTo("델타소프트");
    }

    @Test
    @DisplayName("기존 회사가 있는 사용자가 저장하면 기존 회사 정보가 수정되고 하위 컬렉션이 전체 교체된다")
    void saveProfile_기존회사가있으면_수정되고_하위컬렉션이_전체교체된다() {
        // given
        User user = User.create("owner@bidradar.com", "hash", "홍길동");
        Company existing = Company.create(user, "기존회사");
        CompanyProfileRequest request = emptyRequest("변경된회사");
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(existing));
        given(companyRepository.save(existing)).willReturn(existing);
        given(companyTechTagRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBusinessAreaRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyCertificateRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyProjectExperienceRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBidPreferenceRepository.findByCompanyId(any())).willReturn(Optional.empty());
        given(matchCalculationStatusRepository.findByCompanyId(any())).willReturn(Optional.empty());

        // when
        companyProfileService.saveProfile(1L, request);

        // then
        verify(companyTechTagRepository).deleteAllByCompanyId(any());
        verify(companyBusinessAreaRepository).deleteAllByCompanyId(any());
        verify(companyCertificateRepository).deleteAllByCompanyId(any());
        verify(companyProjectExperienceRepository).deleteAllByCompanyId(any());
        verify(companyBidPreferenceRepository).deleteByCompanyId(any());
        verify(userRepository, never()).getReferenceById(any());
        assertThat(existing.getCompanyName()).isEqualTo("변경된회사");
    }

    @Test
    @DisplayName("이미 계산이 진행 중이고 5분 이내면 전용 CONFLICT 예외가 발생하고 저장이 진행되지 않는다")
    void saveProfile_계산진행중이면_CONFLICT예외() {
        // given
        User user = User.create("owner@bidradar.com", "hash", "홍길동");
        Company existing = Company.create(user, "기존회사");
        MatchCalculationStatus status = MatchCalculationStatus.start(existing, "old-token");
        CompanyProfileRequest request = emptyRequest("변경된회사");
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(existing));
        given(companyRepository.save(existing)).willReturn(existing);
        given(matchCalculationStatusRepository.findByCompanyId(any())).willReturn(Optional.of(status));
        given(matchCalculationStatusRepository.acquireLock(any(), any(), any())).willReturn(0);

        // when // then
        assertThatThrownBy(() -> companyProfileService.saveProfile(1L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getResultCode()).isEqualTo(ResultCode.MATCH_CALCULATION_IN_PROGRESS));

        verify(companyTechTagRepository, never()).deleteAllByCompanyId(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("계산 상태가 IN_PROGRESS가 아니거나 락이 만료됐으면 저장이 성공하고 새 토큰으로 이벤트가 발행된다")
    void saveProfile_락이없거나만료면_저장성공하고_이벤트발행() {
        // given
        User user = User.create("owner@bidradar.com", "hash", "홍길동");
        Company existing = Company.create(user, "기존회사");
        MatchCalculationStatus status = MatchCalculationStatus.start(existing, "old-token");
        CompanyProfileRequest request = emptyRequest("변경된회사");
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(existing));
        given(companyRepository.save(existing)).willReturn(existing);
        given(companyTechTagRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBusinessAreaRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyCertificateRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyProjectExperienceRepository.findByCompanyId(any())).willReturn(List.of());
        given(companyBidPreferenceRepository.findByCompanyId(any())).willReturn(Optional.empty());
        given(matchCalculationStatusRepository.findByCompanyId(any())).willReturn(Optional.of(status));
        given(matchCalculationStatusRepository.acquireLock(any(), any(), any())).willReturn(1);

        // when
        companyProfileService.saveProfile(1L, request);

        // then
        verify(companyTechTagRepository).deleteAllByCompanyId(any());
        verify(eventPublisher).publishEvent(any(CompanyProfileSavedEvent.class));
    }
}
