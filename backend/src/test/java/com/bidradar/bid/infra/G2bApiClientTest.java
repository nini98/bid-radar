package com.bidradar.bid.infra;

import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.config.G2bProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

// Spring Context 없이 실행 — WireMock이 로컬 HTTP 서버 역할을 한다.
// G2bApiClient가 실제로 HTTP 요청을 보내고 응답을 파싱하는 과정을 검증한다.
class G2bApiClientTest {

    static WireMockServer wireMockServer;

    G2bApiClient client;
    final LocalDate TEST_DATE = LocalDate.of(2025, 6, 17);

    @BeforeAll
    static void startServer() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        G2bProperties properties = new G2bProperties();
        properties.setBaseUrl("http://localhost:" + wireMockServer.port() + "/");
        properties.setServiceKey("testkey");
        properties.setPageSize(10);

        client = new G2bApiClient(RestClient.builder(), properties, new ObjectMapper());
    }

    @Test
    @DisplayName("공사 공고 응답을 파싱해서 G2bNoticeItem 목록을 반환한다")
    void 공사_공고_응답_파싱() {
        // given
        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(singleItemResponse("20250001", "서울 도로 공사"))));

        // when
        List<G2bNoticeItem> items = client.fetchConstructionNotices(TEST_DATE);

        // then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).bidNtceNo()).isEqualTo("20250001");
        assertThat(items.get(0).bidNtceNm()).isEqualTo("서울 도로 공사");
    }

    @Test
    @DisplayName("API 오류 응답(resultCode != 00)이면 빈 목록을 반환한다")
    void API_오류_응답이면_빈_목록_반환() {
        // given
        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponse("99", "SERVICE ERROR"))));

        // when
        List<G2bNoticeItem> items = client.fetchConstructionNotices(TEST_DATE);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("totalCount가 0이면 빈 목록을 반환한다")
    void totalCount가_0이면_빈_목록_반환() {
        // given
        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyResponse())));

        // when
        List<G2bNoticeItem> items = client.fetchConstructionNotices(TEST_DATE);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("items 노드가 없으면 빈 목록을 반환한다")
    void items_노드가_없으면_빈_목록_반환() {
        // given
        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(noItemsNodeResponse())));

        // when
        List<G2bNoticeItem> items = client.fetchConstructionNotices(TEST_DATE);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("응답 JSON이 깨진 경우 빈 목록을 반환한다")
    void 응답_JSON이_깨진_경우_빈_목록_반환() {
        // given
        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("not a valid json {")));

        // when
        List<G2bNoticeItem> items = client.fetchConstructionNotices(TEST_DATE);

        // then
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("총 건수보다 첫 페이지 결과가 적으면 다음 페이지를 이어서 호출한다")
    void 페이지네이션_다음_페이지_호출() {
        // given
        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .withQueryParam("pageNo", equalTo("1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(pagedResponse("20250001", "1페이지 공고", 2))));

        wireMockServer.stubFor(get(urlPathMatching(".*getBidPblancListInfoCnstwk.*"))
                .withQueryParam("pageNo", equalTo("2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(pagedResponse("20250002", "2페이지 공고", 2))));

        // when
        List<G2bNoticeItem> items = client.fetchConstructionNotices(TEST_DATE);

        // then
        assertThat(items).hasSize(2);
        assertThat(items.get(0).bidNtceNo()).isEqualTo("20250001");
        assertThat(items.get(1).bidNtceNo()).isEqualTo("20250002");
    }

    private String singleItemResponse(String bidNtceNo, String bidNtceNm) {
        return """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "OK" },
                    "body": {
                      "totalCount": 1,
                      "items": [
                        {
                          "bidNtceNo": "%s",
                          "bidNtceOrd": "00",
                          "bidNtceNm": "%s",
                          "ntceInsttNm": "테스트기관",
                          "bidMethdNm": "전자입찰",
                          "bidNtceDt": "2025-06-17 09:00:00",
                          "bidClseDt": "2025-06-20 18:00:00"
                        }
                      ]
                    }
                  }
                }
                """.formatted(bidNtceNo, bidNtceNm);
    }

    private String pagedResponse(String bidNtceNo, String bidNtceNm, int totalCount) {
        return """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "OK" },
                    "body": {
                      "totalCount": %d,
                      "items": [
                        {
                          "bidNtceNo": "%s",
                          "bidNtceOrd": "00",
                          "bidNtceNm": "%s",
                          "bidNtceDt": "2025-06-17 09:00:00"
                        }
                      ]
                    }
                  }
                }
                """.formatted(totalCount, bidNtceNo, bidNtceNm);
    }

    private String errorResponse(String code, String msg) {
        return """
                {
                  "response": {
                    "header": { "resultCode": "%s", "resultMsg": "%s" },
                    "body": {}
                  }
                }
                """.formatted(code, msg);
    }

    private String emptyResponse() {
        return """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "OK" },
                    "body": { "totalCount": 0 }
                  }
                }
                """;
    }

    private String noItemsNodeResponse() {
        return """
                {
                  "response": {
                    "header": { "resultCode": "00", "resultMsg": "OK" },
                    "body": { "totalCount": 1 }
                  }
                }
                """;
    }
}
