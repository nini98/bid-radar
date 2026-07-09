package com.bidradar.company.domain;

import com.bidradar.auth.domain.User;
import com.bidradar.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "companies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_companies_user"))
    private User user;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "website", length = 300)
    private String website;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_email", length = 255)
    private String managerEmail;

    @Column(name = "manager_phone", length = 20)
    private String managerPhone;

    @Column(name = "narajangter_linked", nullable = false)
    private boolean narajangterLinked;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<CompanyTechTag> companyTechTags = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<CompanyBusinessArea> companyBusinessAreas = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<CompanyCertificate> companyCertificates = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<CompanyProjectExperience> companyProjectExperiences = new ArrayList<>();

    public static Company create(User user, String companyName) {
        Company company = new Company();
        company.user = user;
        company.companyName = companyName;
        company.narajangterLinked = false;
        return company;
    }

    public void update(String companyName,
                        String businessNumber,
                        String industry,
                        Integer foundedYear,
                        String companySize,
                        String region,
                        String address,
                        String website,
                        String strengths,
                        String managerName,
                        String managerEmail,
                        String managerPhone) {
        this.companyName = companyName;
        this.businessNumber = businessNumber;
        this.industry = industry;
        this.foundedYear = foundedYear;
        this.companySize = companySize;
        this.region = region;
        this.address = address;
        this.website = website;
        this.strengths = strengths;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.managerPhone = managerPhone;
    }
}
