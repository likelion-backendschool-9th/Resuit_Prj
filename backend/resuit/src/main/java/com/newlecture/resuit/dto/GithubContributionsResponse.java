package com.newlecture.resuit.dto;

import java.util.List;

public record GithubContributionsResponse(
        String login,
        int totalContributions,
        List<List<Integer>> weeks,
        String from,
        String to
) {
}
