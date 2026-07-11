package com.bidradar.match.service.rule;

import com.bidradar.auth.domain.User;
import com.bidradar.company.domain.Company;

final class RuleTestFixtures {

    private RuleTestFixtures() {}

    static Company company() {
        User user = User.create("owner@bidradar.com", "hash", "홍길동");
        return Company.create(user, "테스트 회사");
    }
}
