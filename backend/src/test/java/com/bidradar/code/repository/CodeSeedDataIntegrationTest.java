package com.bidradar.code.repository;

import com.bidradar.config.JpaConfig;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class CodeSeedDataIntegrationTest extends IntegrationTestBase {

    @Autowired
    TechTagRepository techTagRepository;

    @Autowired
    BusinessAreaRepository businessAreaRepository;

    @Test
    @DisplayName("V11 마이그레이션 적용 후 tech_tags에 시딩 데이터가 존재한다")
    void techTags_시딩데이터가_존재한다() {
        assertThat(techTagRepository.findAll())
                .extracting("name")
                .contains("Java", "Spring", "AWS", "AI");
    }

    @Test
    @DisplayName("V11 마이그레이션 적용 후 business_areas에 시딩 데이터가 존재한다")
    void businessAreas_시딩데이터가_존재한다() {
        assertThat(businessAreaRepository.findAll())
                .extracting("name")
                .contains("SI", "스마트팩토리", "클라우드");
    }
}
