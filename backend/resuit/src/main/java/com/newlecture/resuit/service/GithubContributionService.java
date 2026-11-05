package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.GithubContributionsResponse;

public interface GithubContributionService {
    /** GitHub 로그인 성공 시 액세스 토큰과 login을 integrations에 저장한다. */
    void linkGithubAccount(String resuitUsername, String githubLogin, String accessToken);

    /** 프로필 사용자의 연결된 GitHub 계정 기여 잔디(최근 1년)를 조회한다. */
    GithubContributionsResponse getContributions(String username);
}
