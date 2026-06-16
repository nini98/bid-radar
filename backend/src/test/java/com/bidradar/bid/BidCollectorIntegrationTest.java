package com.bidradar.bid;

import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.bid.service.BidCollectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// 실행 전 환경변수 G2B_SERVICE_KEY에 URL 인코딩된 공공데이터포털 API 키(Encoding key)를 설정해야 한다.
@SpringBootTest
@TestPropertySource(properties = "app.g2b.page-size=5")
class BidCollectorIntegrationTest {

    @Autowired
    BidCollectorService bidCollectorService;

    @Autowired
    BidNoticeRepository bidNoticeRepository;

    @Autowired
    BidAttachmentRepository bidAttachmentRepository;

    @Test
    void 오늘_공고를_수집하면_DB에_저장된다() {
        bidCollectorService.collect(LocalDate.now());

        long noticeCount = bidNoticeRepository.count();
        long attachmentCount = bidAttachmentRepository.count();

        assertThat(noticeCount).isGreaterThan(0);
        System.out.printf("저장된 공고: %d건, 첨부파일: %d건%n", noticeCount, attachmentCount);
    }

    @Test
    void 같은_날_두_번_수집해도_중복저장되지_않는다() {
        bidCollectorService.collect(LocalDate.now());
        long firstCount = bidNoticeRepository.count();

        bidCollectorService.collect(LocalDate.now());
        long secondCount = bidNoticeRepository.count();

        assertThat(secondCount).isEqualTo(firstCount);
    }
}
