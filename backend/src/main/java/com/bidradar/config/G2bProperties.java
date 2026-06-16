package com.bidradar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.g2b")
public class G2bProperties {
    private String baseUrl = "https://apis.data.go.kr/1230000/ad/BidPublicInfoService/";
    private String serviceKey = "";
    private int pageSize = 100;
}
