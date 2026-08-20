package com.bidradar.bid.service;

import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.bid.service.command.BidNoticeCreateCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class G2bNoticeMapperTest {

    private final G2bNoticeMapper mapper = new G2bNoticeMapper(new ObjectMapper());

    @Test
    @DisplayName("건설 공고의 주공종명이 qualificationSummary로 매핑된다")
    void 건설_주공종명_매핑() {
        // given
        G2bNoticeItem item = item("상.하수도설비공사업", null, null);

        // when
        BidNoticeCreateCommand command = mapper.toCommand(item);

        // then
        assertThat(command.qualificationSummary()).isEqualTo("상.하수도설비공사업");
    }

    @Test
    @DisplayName("용역 공고의 공공조달분류명이 qualificationSummary로 매핑된다")
    void 용역_공공조달분류명_매핑() {
        // given
        G2bNoticeItem item = item(null, "건설폐기물처리서비스", null);

        // when
        BidNoticeCreateCommand command = mapper.toCommand(item);

        // then
        assertThat(command.qualificationSummary()).isEqualTo("건설폐기물처리서비스");
    }

    @Test
    @DisplayName("물품 공고의 세부품명이 qualificationSummary로 매핑된다")
    void 물품_세부품명_매핑() {
        // given
        G2bNoticeItem item = item(null, null, "사물함");

        // when
        BidNoticeCreateCommand command = mapper.toCommand(item);

        // then
        assertThat(command.qualificationSummary()).isEqualTo("사물함");
    }

    @Test
    @DisplayName("분류 필드가 모두 없으면 qualificationSummary는 null이다")
    void 분류필드_전부없으면_null() {
        // given
        G2bNoticeItem item = item(null, null, null);

        // when
        BidNoticeCreateCommand command = mapper.toCommand(item);

        // then
        assertThat(command.qualificationSummary()).isNull();
    }

    private G2bNoticeItem item(String mainCnsttyNm, String pubPrcrmntClsfcNm, String dtilPrdctClsfcNoNm) {
        return new G2bNoticeItem(
                "R25BK00000001", "000", "테스트 공고", null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                mainCnsttyNm, pubPrcrmntClsfcNm, dtilPrdctClsfcNoNm);
    }
}
