package com.bidradar.match.service;

import com.bidradar.auth.domain.User;
import com.bidradar.bid.domain.BidNotice;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.bidradar.company.domain.Company;
import com.bidradar.company.repository.CompanyRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecalculateServiceTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    BidNoticeRepository bidNoticeRepository;
    @Mock
    MatchCalculationService matchCalculationService;

    @InjectMocks
    RecalculateService recalculateService;

    private final Company company = Company.create(User.create("owner@bidradar.com", "hash", "홍길동"), "테스트 회사");
    private final BidNotice bidA = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-1", "G2B", "공고 A", null, null, null, null, null, null, null,
            null, null, null, null, null));
    private final BidNotice bidB = BidNotice.create(new BidNoticeCreateCommand(
            "EXT-2", "G2B", "공고 B", null, null, null, null, null, null, null,
            null, null, null, null, null));

    @Test
    @DisplayName("회사의 전체 공고에 대해 매칭을 재계산한다")
    void 전체_공고에_대해_재계산한다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));

        // when
        recalculateService.recalculate(1L);

        // then
        verify(matchCalculationService).calculateAndSave(bidA, company);
        verify(matchCalculationService).calculateAndSave(bidB, company);
    }

    @Test
    @DisplayName("한 공고의 재계산이 실패해도 나머지 공고는 계속 처리된다")
    void 한_공고가_실패해도_나머지는_계속_처리된다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.of(company));
        given(bidNoticeRepository.findAll()).willReturn(List.of(bidA, bidB));
        doThrow(new RuntimeException("계산 실패")).when(matchCalculationService).calculateAndSave(bidA, company);

        // when
        recalculateService.recalculate(1L);

        // then
        verify(matchCalculationService).calculateAndSave(bidB, company);
    }

    @Test
    @DisplayName("회사 프로필이 없으면 재계산을 수행하지 않는다")
    void 회사가_없으면_재계산을_수행하지않는다() {
        // given
        given(companyRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when
        recalculateService.recalculate(1L);

        // then
        verify(matchCalculationService, never()).calculateAndSave(any(), any());
    }
}
