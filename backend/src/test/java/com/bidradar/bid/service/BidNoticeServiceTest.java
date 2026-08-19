package com.bidradar.bid.service;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.dto.query.BidNoticeSearchCondition;
import com.bidradar.bid.dto.response.BidNoticeDetailResponse;
import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.repository.BidSearchCondition;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BidNoticeServiceTest {

    @Mock
    BidNoticeRepository bidNoticeRepository;
    @Mock
    BidAttachmentRepository bidAttachmentRepository;
    @Mock
    CompanyRepository companyRepository;
    @Mock
    BidMatchResultRepository bidMatchResultRepository;
    final BidNoticeMapper bidNoticeMapper = new BidNoticeMapperImpl();

    BidNoticeService service;

    @BeforeEach
    void setUp() {
        service = new BidNoticeService(
                bidNoticeRepository, bidAttachmentRepository, companyRepository, bidMatchResultRepository, bidNoticeMapper
        );
    }

    private final Long userId = 1L;
    private final BidNotice notice = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "테스트 공고", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");

    private final BidNoticeSearchCondition condition = new BidNoticeSearchCondition(
            null, null, null, null, null, null, null, null, null);

    @Test
    @DisplayName("회사 프로필이 없으면 companyId=null로 검색 조건이 구성되고 목록 조회가 정상 동작한다")
    void 회사프로필_없으면_companyId_null로_검색된다() {
        // given
        given(companyRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(bidNoticeRepository.search(any(), any())).willReturn(new PageImpl<>(List.of()));

        // when
        service.getList(condition, userId);

        // then
        ArgumentCaptor<BidSearchCondition> captor = ArgumentCaptor.forClass(BidSearchCondition.class);
        verify(bidNoticeRepository).search(captor.capture(), any());
        assertThat(captor.getValue().companyId()).isNull();
    }

    @Test
    @DisplayName("회사 프로필이 있으면 companyId가 검색 조건에 포함되어 repository에 전달된다")
    void 회사프로필_있으면_companyId가_포함된다() {
        // given
        ReflectionTestUtils.setField(company, "id", 10L);
        given(companyRepository.findByUserId(userId)).willReturn(Optional.of(company));
        given(bidNoticeRepository.search(any(), any())).willReturn(new PageImpl<>(List.of()));

        // when
        service.getList(condition, userId);

        // then
        ArgumentCaptor<BidSearchCondition> captor = ArgumentCaptor.forClass(BidSearchCondition.class);
        verify(bidNoticeRepository).search(captor.capture(), any());
        assertThat(captor.getValue().companyId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("회사 프로필이 없으면 상세 조회 시 matchResult가 null이다")
    void 회사프로필_없으면_상세조회시_matchResult가_null이다() {
        // given
        Long bidId = 1L;
        given(bidNoticeRepository.findById(bidId)).willReturn(Optional.of(notice));
        given(bidAttachmentRepository.findByBidNoticeId(bidId)).willReturn(List.of());
        given(companyRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when
        BidNoticeDetailResponse result = service.getDetail(bidId, userId);

        // then
        verify(bidMatchResultRepository, never()).findByBidNoticeIdAndCompanyId(any(), any());
        assertThat(result.matchResult()).isNull();
    }

    @Test
    @DisplayName("회사 프로필과 매치 결과가 있으면 상세 조회 시 matchResult가 채워진다")
    void 회사프로필과_매치결과가_있으면_matchResult가_채워진다() {
        // given
        Long bidId = 1L;
        ReflectionTestUtils.setField(company, "id", 10L);
        BidMatchResult matchResult = BidMatchResult.create(
                notice, company, new BigDecimal("85.00"), MatchGrade.STRONG_REVIEW,
                null, null, null, null, null, null);
        given(bidNoticeRepository.findById(bidId)).willReturn(Optional.of(notice));
        given(bidAttachmentRepository.findByBidNoticeId(bidId)).willReturn(List.of());
        given(companyRepository.findByUserId(userId)).willReturn(Optional.of(company));
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(bidId, 10L)).willReturn(Optional.of(matchResult));

        // when
        BidNoticeDetailResponse result = service.getDetail(bidId, userId);

        // then
        assertThat(result.matchResult()).isNotNull();
        assertThat(result.matchResult().status()).isEqualTo(BidMatchResultStatus.SUCCESS);
        assertThat(result.matchResult().totalScore()).isEqualByComparingTo("85.00");
        assertThat(result.matchResult().grade()).isEqualTo(MatchGrade.STRONG_REVIEW);
        assertThat(result.matchResult().displayText()).isEqualTo("적극 검토");
    }

    @Test
    @DisplayName("매치 결과가 FAILED면 상세 조회 시 status=FAILED로 그대로 노출된다")
    void 매치결과가_FAILED면_상세조회시_FAILED가_노출된다() {
        // given: 응답 계약에 status가 추가되면서(Issue #40 프론트 노출 Task), FAILED 결과를
        // 더 이상 숨기지 않고 그대로 내려보낸다. 점수 필드는 FAILED일 때 전부 null이다.
        Long bidId = 1L;
        ReflectionTestUtils.setField(company, "id", 10L);
        BidMatchResult failedResult = BidMatchResult.createFailed(notice, company, "RuntimeException: 계산 중 오류");
        given(bidNoticeRepository.findById(bidId)).willReturn(Optional.of(notice));
        given(bidAttachmentRepository.findByBidNoticeId(bidId)).willReturn(List.of());
        given(companyRepository.findByUserId(userId)).willReturn(Optional.of(company));
        given(bidMatchResultRepository.findByBidNoticeIdAndCompanyId(bidId, 10L)).willReturn(Optional.of(failedResult));

        // when
        BidNoticeDetailResponse result = service.getDetail(bidId, userId);

        // then
        assertThat(result.matchResult()).isNotNull();
        assertThat(result.matchResult().status()).isEqualTo(BidMatchResultStatus.FAILED);
        assertThat(result.matchResult().totalScore()).isNull();
        assertThat(result.matchResult().grade()).isNull();
    }
}
