package com.newlecture.resuit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 현직자 인증에 쓸 수 있는 회사 이메일 도메인 목록이다. 예: newlecture.com
// 이 목록에 있는 도메인으로 인증을 마친 회원이 현직자가 될 수 있다. 회사명은 company_name이다.
@Entity
@Table(name = "company_email_domains", uniqueConstraints = @UniqueConstraint(
        name = "uq_company_email_domains_domain", columnNames = "domain"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyEmailDomain extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // '@' 뒤의 도메인만 소문자로 저장한다. 예: newlecture.com
    @Column(name = "domain", nullable = false, length = 255)
    private String domain;
    // 이 도메인의 회사명. 현직자 인증 때 현직자의 회사명으로 쓴다. 비어 있으면 도메인을 그대로 쓴다.
    @Column(name = "company_name", length = 100)
    private String companyName;
}
