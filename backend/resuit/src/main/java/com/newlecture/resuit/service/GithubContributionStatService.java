package com.newlecture.resuit.service;

import java.util.List;

public interface GithubContributionStatService {

    List<String> findSyncTargets();

    void sync(String username);
}
