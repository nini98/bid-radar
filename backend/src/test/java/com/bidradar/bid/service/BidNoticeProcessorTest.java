package com.bidradar.bid.service;

import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.bid.repository.BidAttachmentRepository;
import com.bidradar.bid.repository.BidNoticeRepository;
import com.bidradar.config.JpaConfig;
import com.bidradar.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BidNoticeProcessor.class, G2bNoticeMapper.class, JpaConfig.class})
class BidNoticeProcessorTest extends IntegrationTestBase {

    @Autowired
    BidNoticeProcessor processor;

    @Autowired
    BidNoticeRepository bidNoticeRepository;

    @Autowired
    BidAttachmentRepository bidAttachmentRepository;

    @MockitoBean
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    }

    @Test
    @DisplayName("공고를 처리하면 DB에 저장된다")
    void 공고를_처리하면_DB에_저장된다() {
        // given
        G2bNoticeItem item = createItem("20250001");

        // when
        processor.process(item);

        // then
        assertThat(bidNoticeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 공고를 두 번 처리해도 중복 저장되지 않는다")
    void 같은_공고를_두번_처리해도_중복저장되지않는다() {
        // given
        G2bNoticeItem item = createItem("20250001");

        // when
        processor.process(item);
        processor.process(item);

        // then
        assertThat(bidNoticeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("첨부파일이 있는 공고를 처리하면 첨부파일도 함께 저장된다")
    void 첨부파일이_있는_공고를_처리하면_첨부파일도_저장된다() {
        // given
        G2bNoticeItem item = createItemWithAttachment("20250002");

        // when
        processor.process(item);

        // then
        assertThat(bidNoticeRepository.count()).isEqualTo(1);
        assertThat(bidAttachmentRepository.count()).isEqualTo(1);
    }

    private G2bNoticeItem createItem(String bidNtceNo) {
        return new G2bNoticeItem(
                bidNtceNo, "00", "테스트 공사 공고", "국토교통부",
                "일반경쟁", "일반계약", "50000000", "서울특별시",
                "2025-06-17 09:00:00", null, "2025-06-20 18:00:00", null,
                "https://www.g2b.go.kr/test", "N", "공사", "2025-06-17 09:00:00",
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null
        );
    }

    private G2bNoticeItem createItemWithAttachment(String bidNtceNo) {
        return new G2bNoticeItem(
                bidNtceNo, "00", "첨부파일 포함 공고", "국토교통부",
                "일반경쟁", "일반계약", "50000000", "서울특별시",
                "2025-06-17 09:00:00", null, "2025-06-20 18:00:00", null,
                "https://www.g2b.go.kr/test", "N", "공사", "2025-06-17 09:00:00",
                "https://www.g2b.go.kr/file1.pdf",
                null, null, null, null, null, null, null, null, null,
                "입찰공고문.pdf",
                null, null, null, null, null, null, null, null, null
        );
    }
}
