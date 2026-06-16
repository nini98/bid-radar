package com.bidradar.bid.infra;

import com.bidradar.bid.infra.dto.G2bNoticeItem;
import com.bidradar.config.G2bProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class G2bApiClient {

    private static final String CONSTRUCTION = "getBidPblancListInfoCnstwk";
    private static final String SERVICE = "getBidPblancListInfoServc";
    private static final String GOODS = "getBidPblancListInfoThng";
    private static final DateTimeFormatter DATE_PARAM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestClient restClient;
    private final G2bProperties properties;
    private final ObjectMapper objectMapper;

    public G2bApiClient(RestClient.Builder restClientBuilder, G2bProperties properties, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public List<G2bNoticeItem> fetchConstructionNotices(LocalDate date) {
        return fetchAll(CONSTRUCTION, date);
    }

    public List<G2bNoticeItem> fetchServiceNotices(LocalDate date) {
        return fetchAll(SERVICE, date);
    }

    public List<G2bNoticeItem> fetchGoodsNotices(LocalDate date) {
        return fetchAll(GOODS, date);
    }

    private List<G2bNoticeItem> fetchAll(String operation, LocalDate date) {
        List<G2bNoticeItem> all = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            PageResult result = fetchPage(operation, date, pageNo);
            all.addAll(result.items());
            if (all.size() >= result.totalCount() || result.items().isEmpty()) break;
            pageNo++;
        }

        log.debug("G2B {} 수집 완료: {}건 ({})", operation, all.size(), date);
        return all;
    }

    private PageResult fetchPage(String operation, LocalDate date, int pageNo) {
        String dateStr = date.format(DATE_PARAM_FORMAT);
        String url = properties.getBaseUrl() + operation
                + "?ServiceKey=" + properties.getServiceKey()
                + "&type=json"
                + "&numOfRows=" + properties.getPageSize()
                + "&pageNo=" + pageNo
                + "&inqryDiv=1"
                + "&inqryBgnDt=" + dateStr + "0000"
                + "&inqryEndDt=" + dateStr + "2359";

        String responseBody = restClient.get()
                .uri(URI.create(url))
                .retrieve()
                .body(String.class);

        return parsePageResult(responseBody, operation, date);
    }

    private PageResult parsePageResult(String responseBody, String operation, LocalDate date) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                log.warn("G2B API 오류 응답: operation={}, date={}, code={}, msg={}",
                        operation, date, resultCode, header.path("resultMsg").asText());
                return new PageResult(Collections.emptyList(), 0);
            }

            JsonNode body = root.path("response").path("body");
            int totalCount = body.path("totalCount").asInt(0);
            if (totalCount == 0) return new PageResult(Collections.emptyList(), 0);

            JsonNode itemsNode = body.path("items");
            if (itemsNode.isMissingNode() || itemsNode.isNull() || !itemsNode.isArray()) {
                return new PageResult(Collections.emptyList(), totalCount);
            }

            List<G2bNoticeItem> items = objectMapper.convertValue(itemsNode, new TypeReference<>() {});
            return new PageResult(items, totalCount);

        } catch (Exception e) {
            log.error("G2B API 응답 파싱 실패: operation={}, date={}", operation, date, e);
            return new PageResult(Collections.emptyList(), 0);
        }
    }

    private record PageResult(List<G2bNoticeItem> items, int totalCount) {}
}
