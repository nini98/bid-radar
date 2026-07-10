package com.bidradar.match.service;

import java.util.List;
import java.util.Set;

public record CompanyProfileContext(
        Set<String> techTagNames,
        Set<String> businessAreaNames,
        List<String> preferredRegions,
        Long budgetMin,
        Long budgetMax,
        Integer deadlineMinDays,
        List<String> preferredBidTypes,
        List<String> preferredContractTypes
) {}
