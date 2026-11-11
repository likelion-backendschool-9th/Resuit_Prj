package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.GithubContributionsResponse;
import java.time.LocalDate;

public interface GithubContributionService {
    /** GitHub 로그인 성공 시 액세스 토큰과 login을 integrations에 저장한다. */
    void linkGithubAccount(String resuitUsername, String githubLogin, String accessToken);

    /**
     * 프로필 사용자의 연결된 GitHub 계정 기여 잔디를 조회한다.
     * from·to가 모두 없으면 최근 1년. 둘 다 있으면 해당 기간(최대 1년)을 조회한다.
     */
    GithubContributionsResponse getContributions(String username, LocalDate from, LocalDate to);
}
