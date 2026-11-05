package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.GithubContributionsResponse;
import com.newlecture.resuit.entity.Integration;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.IntegrationRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.GithubContributionService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GithubContributionServiceImpl implements GithubContributionService {
    private static final String GRAPHQL = """
            query($login: String!, $from: DateTime!, $to: DateTime!) {
              user(login: $login) {
                login
                contributionsCollection(from: $from, to: $to) {
                  contributionCalendar {
                    totalContributions
                    weeks {
                      contributionDays {
                        date
                        contributionCount
                        contributionLevel
                      }
                    }
                  }
                }
              }
            }
            """;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final UserRepository users;
    private final IntegrationRepository integrations;
    private final RestClient githubApi = RestClient.builder()
            .baseUrl("https://api.github.com")
            .requestFactory(new JdkClientHttpRequestFactory())
            .build();

    @Override
    @Transactional
    public void linkGithubAccount(String resuitUsername, String githubLogin, String accessToken) {
        if (resuitUsername == null || githubLogin == null || accessToken == null
                || githubLogin.isBlank() || accessToken.isBlank()) {
            return;
        }
        User user = users.findByUsername(resuitUsername).orElse(null);
        if (user == null) return;
        Integration integration = integrations.findByUserIdAndProvider(user.getId(), "GITHUB")
                .orElseGet(() -> integrations.save(Integration.builder()
                        .user(user).provider("GITHUB").build()));
        integration.connectGithub(githubLogin, accessToken);
        integration.markSynced(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul")));
    }

    @Override
    @Transactional(readOnly = true)
    public GithubContributionsResponse getContributions(String username) {
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Integration linked = integrations.findByUserIdAndProvider(user.getId(), "GITHUB")
                .filter(Integration::isConnected)
                .filter(item -> item.getExternalId() != null && item.getAccessToken() != null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "GitHub 연동이 없습니다. GitHub로 다시 로그인해 주세요."));

        ZonedDateTime to = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime from = to.minusYears(1);
        Map<String, Object> body = Map.of(
                "query", GRAPHQL,
                "variables", Map.of(
                        "login", linked.getExternalId(),
                        "from", Instant.from(from).toString(),
                        "to", Instant.from(to).toString()
                )
        );

        Map<String, Object> root;
        try {
            root = githubApi.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + linked.getAccessToken())
                    .header("Accept", "application/vnd.github+json")
                    .body(body)
                    .retrieve()
                    .body(MAP_TYPE);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 기여 기록을 가져오지 못했습니다.", ex);
        }

        if (root == null || root.get("errors") != null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 기여 기록 조회에 실패했습니다.");
        }

        Map<String, Object> data = castMap(root.get("data"));
        Map<String, Object> githubUser = castMap(data == null ? null : data.get("user"));
        Map<String, Object> collection = castMap(githubUser == null ? null : githubUser.get("contributionsCollection"));
        Map<String, Object> calendar = castMap(collection == null ? null : collection.get("contributionCalendar"));
        if (calendar == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GitHub 기여 기록이 없습니다.");
        }

        int total = ((Number) calendar.getOrDefault("totalContributions", 0)).intValue();
        List<List<Integer>> weeks = new ArrayList<>();
        Object weeksNode = calendar.get("weeks");
        if (weeksNode instanceof List<?> weekList) {
            for (Object weekObj : weekList) {
                Map<String, Object> week = castMap(weekObj);
                List<Integer> days = new ArrayList<>(7);
                Object daysNode = week == null ? null : week.get("contributionDays");
                if (daysNode instanceof List<?> dayList) {
                    for (Object dayObj : dayList) {
                        Map<String, Object> day = castMap(dayObj);
                        String level = day == null || day.get("contributionLevel") == null
                                ? "" : day.get("contributionLevel").toString();
                        int count = day == null || !(day.get("contributionCount") instanceof Number number)
                                ? 0 : number.intValue();
                        days.add(levelOf(level, count));
                    }
                }
                while (days.size() < 7) days.add(0);
                weeks.add(days);
            }
        }
        return new GithubContributionsResponse(linked.getExternalId(), total, weeks);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private static int levelOf(String level, int count) {
        return switch (level) {
            case "NONE" -> 0;
            case "FIRST_QUARTILE" -> 1;
            case "SECOND_QUARTILE" -> 2;
            case "THIRD_QUARTILE" -> 3;
            case "FOURTH_QUARTILE" -> 4;
            default -> count <= 0 ? 0 : count < 4 ? 1 : count < 7 ? 2 : count < 10 ? 3 : 4;
        };
    }
}
