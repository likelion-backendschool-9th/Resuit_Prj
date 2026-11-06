package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.config.BlogVerifySession;
import com.newlecture.resuit.dto.BlogIntegrationRequest;
import com.newlecture.resuit.dto.BlogPostResponse;
import com.newlecture.resuit.dto.BlogVerifyStartResponse;
import com.newlecture.resuit.dto.IntegrationResponse;
import com.newlecture.resuit.dto.PinnedReposRequest;
import com.newlecture.resuit.dto.RepoResponse;
import com.newlecture.resuit.entity.Integration;
import com.newlecture.resuit.entity.PinnedRepo;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.BlogPostRepository;
import com.newlecture.resuit.repository.IntegrationRepository;
import com.newlecture.resuit.repository.PinnedRepoRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.BlogRssCollector;
import com.newlecture.resuit.service.ProfileIntegrationService;
import com.newlecture.resuit.service.TokenCipher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileIntegrationServiceImpl implements ProfileIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(ProfileIntegrationServiceImpl.class);
    private static final ParameterizedTypeReference<List<Map<String, Object>>> REPO_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final UserRepository users;
    private final IntegrationRepository integrations;
    private final BlogPostRepository blogPosts;
    private final PinnedRepoRepository pinnedRepos;
    private final TokenCipher tokenCipher;
    private final BlogRssCollector blogRssCollector;
    private final RestClient githubApi = RestClient.builder()
            .baseUrl("https://api.github.com")
            .requestFactory(new JdkClientHttpRequestFactory())
            .build();

    @Value("${resuit.integration.token-secret:}")
    private String linkSecret;

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationResponse> getIntegrations(UUID userId) {
        return integrations.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public IntegrationResponse connectBlog(UUID userId, BlogIntegrationRequest request) {
        throw badRequest("블로그 소유 검증이 필요합니다. 검증 코드를 발급받은 뒤 확인 API로 연결하세요.");
    }

    @Override
    @Transactional(readOnly = true)
    public BlogVerifyStartResponse startBlogVerify(UUID userId, BlogIntegrationRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        BlogTarget target = resolveBlogTarget(request);
        blogRssCollector.probeFeed(target.rssUrl());
        User user = findUser(userId);
        String code = BlogVerifySession.newCode();
        long expiresAt = Instant.now().toEpochMilli() + BlogVerifySession.MAX_AGE_SECONDS * 1000L;
        BlogVerifySession.Pending pending = new BlogVerifySession.Pending(
                user.getUsername(), target.platform(), target.blogId(), target.rssUrl(), code, expiresAt);
        BlogVerifySession.store(httpRequest, httpResponse, pending, linkSecret);
        String hint = switch (target.platform()) {
            case "VELOG" -> "Velog 설정 → 소개(한 줄 소개 또는 자기소개) 또는 최신 글에 아래 코드를 넣은 뒤 확인해 주세요.";
            case "TISTORY" -> "티스토리 블로그 소개/관리 또는 최신 글에 아래 코드를 넣은 뒤 확인해 주세요.";
            default -> "블로그 소개·About·최신 글 또는 RSS 본문에 아래 코드를 넣은 뒤 확인해 주세요.";
        };
        return new BlogVerifyStartResponse(code, Instant.ofEpochMilli(expiresAt),
                target.platform(), target.blogId(), target.rssUrl(), hint);
    }

    @Override
    @Transactional
    public IntegrationResponse confirmBlogVerify(UUID userId,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = findUser(userId);
        BlogVerifySession.Pending pending = BlogVerifySession.read(httpRequest, linkSecret);
        if (pending == null) {
            throw badRequest("유효한 블로그 검증 요청이 없습니다. 코드를 다시 발급해 주세요.");
        }
        if (!pending.username().equals(user.getUsername())) {
            throw badRequest("다른 계정의 검증 요청입니다. 다시 로그인한 뒤 시도해 주세요.");
        }
        if (!codeFoundOnBlog(pending)) {
            throw badRequest("블로그에서 검증 코드를 찾지 못했습니다. 소개/글에 코드를 넣은 뒤 다시 확인해 주세요.");
        }
        Integration integration = integrations.findByUserIdAndProvider(userId, "BLOG")
                .orElseGet(() -> integrations.save(Integration.builder()
                        .user(user).provider("BLOG").build()));
        integration.connectBlog(pending.platform(), pending.blogId(), pending.rssUrl());
        integrations.flush();
        try {
            blogRssCollector.collect(integration.getId());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("블로그 연결 후 RSS 수집 실패: url={}", pending.rssUrl(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "블로그 RSS를 읽을 수 없습니다. 아이디나 주소를 확인해 주세요.", ex);
        }
        BlogVerifySession.clear(httpRequest, httpResponse);
        return toResponse(integration);
    }

    @Override
    @Transactional
    public IntegrationResponse connectTistory(UUID userId, String blogName) {
        if (blogName == null || !blogName.matches("[A-Za-z0-9][A-Za-z0-9-]{0,59}")) {
            throw badRequest("티스토리 블로그 이름이 올바르지 않습니다.");
        }
        String rssUrl = "https://" + blogName + ".tistory.com/rss";
        blogRssCollector.probeFeed(rssUrl);
        for (Integration other : integrations.findByProviderAndPlatformAndExternalId("BLOG", "TISTORY", blogName)) {
            if (!other.getUser().getId().equals(userId)) {
                other.disconnect();
            }
        }
        User user = findUser(userId);
        Integration integration = integrations.findByUserIdAndProvider(userId, "BLOG")
                .orElseGet(() -> integrations.save(Integration.builder()
                        .user(user).provider("BLOG").build()));
        integration.connectBlog("TISTORY", blogName, rssUrl);
        integrations.flush();
        try {
            blogRssCollector.collect(integration.getId());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("티스토리 연결 후 RSS 수집 실패: blog={}", blogName, ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "티스토리 RSS를 읽을 수 없습니다. 블로그가 공개인지 확인해 주세요.", ex);
        }
        return toResponse(integration);
    }

    private boolean codeFoundOnBlog(BlogVerifySession.Pending pending) {
        String code = pending.code();
        try {
            if (blogRssCollector.fetchText(pending.rssUrl()).contains(code)) {
                return true;
            }
        } catch (ResponseStatusException ignored) {
            /* 프로필 페이지도 검사 */
        }
        if ("VELOG".equals(pending.platform())) {
            try {
                if (blogRssCollector.velogProfileText(pending.blogId()).contains(code)) {
                    return true;
                }
            } catch (ResponseStatusException ignored) {
                /* HTML 프로필도 검사 */
            }
        }
        String profileUrl = profilePageUrl(pending.platform(), pending.blogId(), pending.rssUrl());
        if (profileUrl == null) return false;
        try {
            return blogRssCollector.fetchText(profileUrl).contains(code);
        } catch (ResponseStatusException ex) {
            return false;
        }
    }

    private static String profilePageUrl(String platform, String blogId, String rssUrl) {
        return switch (platform) {
            case "VELOG" -> "https://velog.io/@" + blogId;
            case "TISTORY" -> "https://" + blogId + ".tistory.com";
            case "ETC" -> {
                URI uri = URI.create(rssUrl);
                String path = uri.getPath() == null ? "" : uri.getPath();
                if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
                int slash = path.lastIndexOf('/');
                String basePath = slash > 0 ? path.substring(0, slash) : "";
                yield uri.getScheme() + "://" + uri.getHost()
                        + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                        + basePath;
            }
            default -> null;
        };
    }

    private BlogTarget resolveBlogTarget(BlogIntegrationRequest request) {
        if (request.platform() == null || request.platform().isBlank()) {
            throw badRequest("블로그 플랫폼이 필요합니다.");
        }
        String platform = request.platform().toUpperCase(Locale.ROOT);
        String blogId;
        String rssUrl;
        switch (platform) {
            case "VELOG", "TISTORY" -> {
                if (request.blogId() == null || request.blogId().isBlank()) {
                    throw badRequest("블로그 아이디가 필요합니다.");
                }
                blogId = normalizeBlogId(platform, request.blogId());
                if (!blogId.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,59}")) {
                    throw badRequest("블로그 아이디 형식이 올바르지 않습니다.");
                }
                rssUrl = platform.equals("VELOG")
                        ? "https://v2.velog.io/rss/" + blogId
                        : "https://" + blogId + ".tistory.com/rss";
            }
            case "ETC" -> {
                if (request.rssUrl() == null || request.rssUrl().isBlank()) {
                    throw badRequest("RSS 주소가 필요합니다.");
                }
                rssUrl = request.rssUrl().trim();
                blogRssCollector.assertAllowedRssUrl(rssUrl);
                blogId = URI.create(rssUrl).getHost();
                if (blogId == null || blogId.isBlank()) {
                    throw badRequest("RSS 주소 형식이 올바르지 않습니다.");
                }
                if (blogId.length() > 100) {
                    blogId = blogId.substring(0, 100);
                }
            }
            default -> throw badRequest("지원하지 않는 블로그 플랫폼입니다.");
        }
        blogRssCollector.assertAllowedRssUrl(rssUrl);
        return new BlogTarget(platform, blogId, rssUrl);
    }

    private record BlogTarget(String platform, String blogId, String rssUrl) {}

    /** 아이디, @아이디, 프로필 주소를 블로그 아이디로 맞춘다. */
    private static String normalizeBlogId(String platform, String raw) {
        String value = raw.trim();
        if (value.startsWith("https://") || value.startsWith("http://")) {
            try {
                URI uri = URI.create(value);
                if ("VELOG".equals(platform)) {
                    String path = uri.getPath() == null ? "" : uri.getPath();
                    int at = path.indexOf('@');
                    if (at >= 0) {
                        value = path.substring(at + 1);
                        int slash = value.indexOf('/');
                        if (slash >= 0) value = value.substring(0, slash);
                    }
                } else if ("TISTORY".equals(platform) && uri.getHost() != null
                        && uri.getHost().toLowerCase(Locale.ROOT).endsWith(".tistory.com")) {
                    String host = uri.getHost().toLowerCase(Locale.ROOT);
                    value = host.substring(0, host.length() - ".tistory.com".length());
                }
            } catch (IllegalArgumentException ignored) {
                /* 아래 형식 검사에서 거절 */
            }
        }
        if (value.startsWith("@")) value = value.substring(1);
        return value.trim();
    }

    @Override
    @Transactional
    public IntegrationResponse syncGithub(UUID userId) {
        Integration integration = integrations.findByUserIdAndProvider(userId, "GITHUB")
                .filter(Integration::isConnected)
                .filter(item -> item.getAccessToken() != null && !item.getAccessToken().isBlank())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "GitHub 연동이 없습니다. GitHub로 다시 로그인해 주세요."));
        String accessToken = tokenCipher.decrypt(integration.getAccessToken());

        Map<String, Object> remote;
        try {
            remote = githubApi.get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "GitHub 토큰이 만료되었거나 권한이 없습니다. GitHub로 다시 로그인해 주세요.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 동기화에 실패했습니다.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 동기화에 실패했습니다.");
        }

        String login = remote == null ? null : stringValue(remote.get("login"));
        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 계정 정보를 확인하지 못했습니다.");
        }

        integration.connectGithub(login, tokenCipher.encrypt(accessToken));
        integration.markSynced(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        return toResponse(integration);
    }

    @Override
    @Transactional
    public IntegrationResponse syncBlog(UUID userId) {
        Integration integration = integrations.findByUserIdAndProvider(userId, "BLOG")
                .filter(Integration::isConnected)
                .filter(item -> item.getRssUrl() != null && !item.getRssUrl().isBlank())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "블로그 연동이 없습니다."));
        try {
            blogRssCollector.collect(integration.getId());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "블로그 RSS 동기화에 실패했습니다.", ex);
        }
        return toResponse(integration);
    }

    @Override
    @Transactional
    public IntegrationResponse disconnect(UUID userId, String provider) {
        String normalized = provider.toUpperCase(Locale.ROOT);
        if (!normalized.equals("BLOG") && !normalized.equals("GITHUB")) {
            throw badRequest("지원하지 않는 연동 종류입니다.");
        }
        Integration integration = integrations.findByUserIdAndProvider(userId, normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "연동 내역이 없습니다."));
        // 행은 유지하고 connected=false, access_token=null만 바꾼다(ERD 5.17).
        integration.disconnect();
        return toResponse(integration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPostResponse> getBlogPosts(String username, int size) {
        if (size < 1 || size > 20) throw badRequest("size는 1~20이어야 합니다.");
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return blogPosts.findByUserIdOrderByPublishedAtDesc(user.getId(), PageRequest.of(0, size))
                .stream().map(post -> new BlogPostResponse(post.getId(), post.getTitle(),
                        post.getDescription(), post.getUrl(), post.getPublishedAt())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepoResponse> getGithubRepos(String username, Boolean pinnedOnly) {
        if (Boolean.TRUE.equals(pinnedOnly)) {
            return getPinnedRepos(username);
        }
        return getPublicRepos(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepoResponse> getPinnedRepos(String username) {
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return pinnedRepos.findByUserIdOrderByOrderNoAsc(user.getId()).stream()
                .map(repo -> new RepoResponse(repo.getRepoFullName(),
                        "https://github.com/" + repo.getRepoFullName(), null, null, true))
                .toList();
    }

    @Override
    @Transactional
    public List<RepoResponse> setPinnedRepos(UUID userId, PinnedReposRequest request) {
        List<String> names = request.repoFullNames();
        if (names == null || names.size() > 3 || names.stream().distinct().count() != names.size()
                || names.stream().anyMatch(name -> name == null
                        || !name.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) {
            throw badRequest("대표 레포는 중복 없이 최대 3개까지 선택할 수 있습니다.");
        }
        pinnedRepos.deleteByUserId(userId);
        pinnedRepos.flush();
        User user = findUser(userId);
        for (int i = 0; i < names.size(); i++) {
            pinnedRepos.save(PinnedRepo.builder().user(user)
                    .repoFullName(names.get(i)).orderNo(i + 1).build());
        }
        return getPinnedRepos(user.getUsername());
    }

    private List<RepoResponse> getPublicRepos(String username) {
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Integration linked = integrations.findByUserIdAndProvider(user.getId(), "GITHUB")
                .filter(Integration::isConnected)
                .orElse(null);
        if (linked == null || linked.getAccessToken() == null || linked.getAccessToken().isBlank()
                || linked.getExternalId() == null || linked.getExternalId().isBlank()) {
            return List.of();
        }
        String accessToken = tokenCipher.decrypt(linked.getAccessToken());

        Set<String> pinnedNames = new HashSet<>(
                pinnedRepos.findByUserIdOrderByOrderNoAsc(user.getId()).stream()
                        .map(PinnedRepo::getRepoFullName)
                        .toList());

        List<Map<String, Object>> remote;
        try {
            remote = githubApi.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{login}/repos")
                            .queryParam("type", "owner")
                            .queryParam("sort", "updated")
                            .queryParam("per_page", 100)
                            .build(linked.getExternalId()))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(REPO_LIST_TYPE);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 공개 레포를 불러오지 못했습니다.");
        }

        if (remote == null || remote.isEmpty()) {
            return List.of();
        }

        return remote.stream()
                .filter(repo -> !Boolean.TRUE.equals(repo.get("private")))
                .map(repo -> {
                    String fullName = stringValue(repo.get("full_name"));
                    String url = stringValue(repo.get("html_url"));
                    if (url == null && fullName != null) {
                        url = "https://github.com/" + fullName;
                    }
                    return new RepoResponse(
                            fullName,
                            url,
                            stringValue(repo.get("description")),
                            stringValue(repo.get("language")),
                            fullName != null && pinnedNames.contains(fullName));
                })
                .filter(repo -> repo.fullName() != null && !repo.fullName().isBlank())
                .toList();
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private User findUser(UUID userId) {
        return users.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private IntegrationResponse toResponse(Integration integration) {
        return new IntegrationResponse(integration.getProvider(), integration.getPlatform(),
                integration.getExternalId(), integration.isConnected(), integration.getLastSyncedAt());
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
