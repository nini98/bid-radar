package com.bidradar.code.repository;

import com.bidradar.common.config.ClockConfig;
import com.bidradar.config.JpaConfig;
import com.bidradar.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, ClockConfig.class})
class CodeSeedDataIntegrationTest extends IntegrationTestBase {

    private static final Sort ID_ASC = Sort.by(Sort.Direction.ASC, "id");

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

    @Test
    @DisplayName("tech_tags를 id 오름차순으로 조회하면 시딩 순서와 동일하다")
    void techTags_id_오름차순_조회시_시딩순서와_동일하다() {
        assertThat(techTagRepository.findAll(ID_ASC))
                .extracting("name")
                .containsExactly(
                        "Java", "Spring", "Spring Boot", "Python", "React", "Vue.js", "Node.js",
                        "AWS", "Azure", "Docker", "Kubernetes", "MSA", "QueryDSL",
                        "PostgreSQL", "MySQL", "Oracle", "AI", "빅데이터");
    }

    @Test
    @DisplayName("business_areas를 id 오름차순으로 조회하면 시딩 순서와 동일하다")
    void businessAreas_id_오름차순_조회시_시딩순서와_동일하다() {
        assertThat(businessAreaRepository.findAll(ID_ASC))
                .extracting("name")
                .containsExactly(
                        "SI", "SM(유지보수)", "공공SI", "스마트팩토리", "관제시스템",
                        "클라우드", "AI", "빅데이터", "IoT", "정보보안");
    }
}
