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

// 이 목록에 있는 도메인으로 기업 이메일 인증을 마친 회원만 role이 COMPANY가 된다.
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
}
