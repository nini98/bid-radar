package com.bidradar.company.event;

public record CompanyProfileSavedEvent(Long companyId, String lockToken) {
}
