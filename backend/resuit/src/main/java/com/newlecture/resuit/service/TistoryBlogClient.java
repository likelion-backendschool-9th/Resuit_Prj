package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.TistoryBlogChoice;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TistoryBlogClient {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient http = RestClient.builder().build();
    private final HttpClient tokenHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public TistoryBlogClient(
            @Value("${TISTORY_CLIENT_ID:}") String clientId,
            @Value("${TISTORY_CLIENT_SECRET:}") String clientSecret,
            @Value("${resuit.auth.public-base-url:http://localhost:5173}") String base) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.clientSecret = clientSecret == null ? "" : clientSecret.trim();
        this.redirectUri = base.replaceAll("/+$", "") + "/api/login/tistory/callback";
    }

    public boolean enabled() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public String authorizationUrl(String state) {
        return "https://www.tistory.com/oauth/authorize?client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&response_type=code&state=" + enc(state);
    }

    public String exchangeCode(String code) {
        String url = "https://www.tistory.com/oauth/access_token?client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&redirect_uri=" + enc(redirectUri)
                + "&code=" + enc(code)
                + "&grant_type=authorization_code";
        try {
            HttpResponse<String> response = tokenHttp.send(
                    HttpRequest.newBuilder(URI.create(url))
                            .timeout(Duration.ofSeconds(5))
                            .header("Accept", "text/plain, application/json")
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("tistory_token");
            }
            String token = accessToken(response.body());
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("tistory_token");
            }
            return token;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("tistory_token", ex);
        }
    }

    public List<TistoryBlogChoice> listBlogs(String accessToken) {
        Map<String, Object> root = http.get()
                .uri("https://www.tistory.com/apis/blog/info?access_token={token}&output=json", accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (root == null || !(root.get("tistory") instanceof Map<?, ?> tistory)) {
            throw new IllegalStateException("tistory_blogs");
        }
        if (!(tistory.get("item") instanceof Map<?, ?> item)) {
            throw new IllegalStateException("tistory_blogs");
        }
        List<TistoryBlogChoice> blogs = new ArrayList<>();
        addBlogs(blogs, item.get("blogs"));
        if (blogs.isEmpty()) {
            throw new IllegalStateException("tistory_blogs");
        }
        return blogs;
    }

    private static void addBlogs(List<TistoryBlogChoice> blogs, Object value) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addOne(blogs, item);
            }
            return;
        }
        addOne(blogs, value);
    }

    private static void addOne(List<TistoryBlogChoice> blogs, Object value) {
        if (!(value instanceof Map<?, ?> blog)) return;
        Object name = blog.get("name");
        if (name == null || name.toString().isBlank()) return;
        Object title = blog.get("title");
        String label = title == null || title.toString().isBlank() ? name.toString() : title.toString();
        blogs.add(new TistoryBlogChoice(name.toString(), label));
    }

    private static String accessToken(String body) {
        if (body == null) return null;
        String trimmed = body.trim();
        if (trimmed.startsWith("{")) return null;
        for (String part : trimmed.split("&")) {
            int eq = part.indexOf('=');
            if (eq < 1) continue;
            if ("access_token".equals(part.substring(0, eq))) {
                return part.substring(eq + 1).trim();
            }
        }
        return null;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
