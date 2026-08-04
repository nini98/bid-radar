package com.bidradar.company.domain;

import com.bidradar.auth.domain.User;
import com.bidradar.code.domain.BusinessArea;
import com.bidradar.code.domain.TechTag;
import com.bidradar.common.config.ClockConfig;
import com.bidradar.config.JpaConfig;
import com.bidradar.support.IntegrationTestBase;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, ClockConfig.class})
class CompanyEntityMappingTest extends IntegrationTestBase {

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("Company 저장 시 하위 컬렉션(TechTag/BusinessArea/자격증/프로젝트경험)이 함께 저장되고 조회된다")
    void Company_저장시_하위_컬렉션이_함께_저장되고_조회된다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));

        TechTag techTag = entityManager.persistAndFlush(TechTag.create("백엔드 개발"));
        BusinessArea businessArea = entityManager.persistAndFlush(BusinessArea.create("소프트웨어 개발"));

        entityManager.persistAndFlush(CompanyTechTag.create(company, techTag));
        entityManager.persistAndFlush(CompanyBusinessArea.create(company, businessArea));
        entityManager.persistAndFlush(CompanyCertificate.create(company, "ISO 27001"));
        entityManager.persistAndFlush(CompanyProjectExperience.create(company, "공공", "행정망 구축 사업"));

        // when
        entityManager.clear();
        Company found = entityManager.find(Company.class, company.getId());

        // then
        assertThat(found.getCompanyTechTags()).extracting(t -> t.getTechTag().getName())
                .containsExactly("백엔드 개발");
        assertThat(found.getCompanyBusinessAreas()).extracting(a -> a.getBusinessArea().getName())
                .containsExactly("소프트웨어 개발");
        assertThat(found.getCompanyCertificates()).extracting(CompanyCertificate::getCertificateName)
                .containsExactly("ISO 27001");
        assertThat(found.getCompanyProjectExperiences()).extracting(CompanyProjectExperience::getDescription)
                .containsExactly("행정망 구축 사업");
    }

    @Test
    @DisplayName("한 User가 두 개의 Company를 가지면 UNIQUE 제약 위반이 발생한다")
    void User당_Company_UNIQUE_제약이_동작한다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner2@bidradar.com", "hash", "홍길동"));
        entityManager.persistAndFlush(Company.create(user, "첫번째 회사"));

        // when // then
        assertThatThrownBy(() -> entityManager.persistAndFlush(Company.create(user, "두번째 회사")))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("같은 Company에 같은 TechTag를 두 번 연결하면 UNIQUE 제약 위반이 발생한다")
    void CompanyTechTag_UNIQUE_제약이_동작한다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner3@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));
        TechTag techTag = entityManager.persistAndFlush(TechTag.create("프론트엔드 개발"));
        entityManager.persistAndFlush(CompanyTechTag.create(company, techTag));

        // when // then
        assertThatThrownBy(() -> entityManager.persistAndFlush(CompanyTechTag.create(company, techTag)))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("같은 Company에 같은 BusinessArea를 두 번 연결하면 UNIQUE 제약 위반이 발생한다")
    void CompanyBusinessArea_UNIQUE_제약이_동작한다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner4@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));
        BusinessArea businessArea = entityManager.persistAndFlush(BusinessArea.create("IT 컨설팅"));
        entityManager.persistAndFlush(CompanyBusinessArea.create(company, businessArea));

        // when // then
        assertThatThrownBy(() -> entityManager.persistAndFlush(CompanyBusinessArea.create(company, businessArea)))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("한 Company에 두 개의 CompanyBidPreference를 저장하면 UNIQUE 제약 위반이 발생한다")
    void CompanyBidPreference_UNIQUE_제약이_동작한다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner5@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));
        entityManager.persistAndFlush(CompanyBidPreference.create(
                company, List.of("서울"), 1_000_000L, 100_000_000L, 7, List.of("일반경쟁"), List.of("일반계약")));

        // when // then
        assertThatThrownBy(() -> entityManager.persistAndFlush(CompanyBidPreference.create(
                company, List.of("경기"), null, null, null, null, null)))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("CompanyBidPreference의 배열 컬럼은 저장 후 조회해도 값이 그대로 유지된다")
    void CompanyBidPreference_배열_컬럼이_저장후_유지된다() {
        // given
        User user = entityManager.persistAndFlush(User.create("owner6@bidradar.com", "hash", "홍길동"));
        Company company = entityManager.persistAndFlush(Company.create(user, "테스트 회사"));
        CompanyBidPreference preference = entityManager.persistAndFlush(CompanyBidPreference.create(
                company,
                List.of("서울", "경기"),
                5_000_000L,
                500_000_000L,
                14,
                List.of("일반경쟁", "지명경쟁"),
                List.of("일반계약")));

        // when
        entityManager.clear();
        CompanyBidPreference found = entityManager.find(CompanyBidPreference.class, preference.getId());

        // then
        assertThat(found.getPreferredRegions()).containsExactly("서울", "경기");
        assertThat(found.getPreferredBidTypes()).containsExactly("일반경쟁", "지명경쟁");
        assertThat(found.getPreferredContractTypes()).containsExactly("일반계약");
    }
}
