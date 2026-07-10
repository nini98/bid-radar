package com.bidradar.match.service.rule;

import com.bidradar.bid.domain.BidNotice;
import com.bidradar.company.domain.Company;
import com.bidradar.match.service.CompanyProfileContext;

public interface ScoreRule {

    ScoreResult calculate(BidNotice bid, Company company, CompanyProfileContext profile);
}
