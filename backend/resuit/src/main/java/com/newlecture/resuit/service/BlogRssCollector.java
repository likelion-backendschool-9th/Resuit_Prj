package com.newlecture.resuit.service;

import com.newlecture.resuit.entity.BlogPost;
import com.newlecture.resuit.entity.Integration;
import com.newlecture.resuit.repository.BlogPostRepository;
import com.newlecture.resuit.repository.IntegrationRepository;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class BlogRssCollector {
    private final IntegrationRepository integrations;
    private final BlogPostRepository posts;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public BlogRssCollector(IntegrationRepository integrations, BlogPostRepository posts) {
        this.integrations = integrations;
        this.posts = posts;
    }

    /** 연결·수집 전에 HTTPS·사설망 SSRF를 검사한다. */
    public void assertAllowedRssUrl(String rssUrl) {
        URI feed = parseHttpsUri(rssUrl);
        assertPublicHost(feed.getHost());
    }

    public void assertAllowedPageUrl(String pageUrl) {
        URI page = parseHttpsUri(pageUrl);
        assertPublicHost(page.getHost());
    }

    /** SSRF-safe HTTPS GET. 공개 HTTPS 리다이렉트만 최대 3번 따라간다. */
    public String fetchText(String url) {
        return fetchText(url, 0);
    }

    /**
     * 벨로그 소개는 프로필 HTML에 없다. 공개 GraphQL의 소개·한 줄 소개를 읽는다.
     */
    public String velogProfileText(String blogId) {
        if (blogId == null || !blogId.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,59}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "블로그 아이디 형식이 올바르지 않습니다.");
        }
        String body = "{\"query\":\"query($username:String!){ user(username:$username){ profile { display_name short_bio about } } }\",\"variables\":{\"username\":\""
                + blogId + "\"}}";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://v2.velog.io/graphql"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "ResuitBlogVerify/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "벨로그 프로필을 읽지 못했습니다.");
            }
            return response.body();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "벨로그 프로필을 읽지 못했습니다.", ex);
        }
    }

    private String fetchText(String url, int hops) {
        assertAllowedPageUrl(url);
        URI target = parseHttpsUri(url);
        try {
            HttpRequest request = HttpRequest.newBuilder(target)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .header("Accept", "text/html, application/rss+xml, application/xml, text/xml, */*")
                    .header("User-Agent", "ResuitBlogVerify/1.0")
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                if (hops >= 3) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RSS 주소 리다이렉트가 너무 많습니다.");
                }
                String location = response.headers().firstValue("location").orElse(null);
                if (location == null || location.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주소를 읽을 수 없습니다. (HTTP " + status + ")");
                }
                return fetchText(target.resolve(location).toString(), hops + 1);
            }
            if (status < 200 || status >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "주소를 읽을 수 없습니다. (HTTP " + status + ")");
            }
            String body = response.body();
            if (body == null || body.length() > 1_000_000) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "응답 크기가 올바르지 않습니다.");
            }
            return body;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주소를 읽을 수 없습니다.", ex);
        }
    }

    /** RSS 피드가 실제로 열리는지 확인한다. */
    public void probeFeed(String rssUrl) {
        String xml = fetchText(rssUrl);
        String lower = xml.toLowerCase();
        if (!(lower.contains("<rss") || lower.contains("<feed") || lower.contains("<channel"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효한 RSS 피드가 아닙니다.");
        }
    }

    @Transactional
    public void collect(Long integrationId) {
        Integration integration = integrations.findById(integrationId).orElseThrow();
        if (!integration.isConnected() || !"BLOG".equals(integration.getProvider())) return;
        String rssUrl = integration.getRssUrl();
        String xml = fetchText(rssUrl);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            NodeList items = document.getElementsByTagName("item");
            if (items.getLength() == 0) {
                items = document.getElementsByTagName("entry");
            }
            for (int i = 0; i < Math.min(items.getLength(), 50); i++) {
                Element item = (Element) items.item(i);
                String title = childText(item, "title");
                String link = linkOf(item);
                String guid = firstText(item, "guid", "id");
                if (guid == null || guid.isBlank()) guid = link;
                if (title == null || link == null || guid == null
                        || !link.startsWith("https://")) continue;
                guid = truncate(guid, 500);
                if (posts.existsByIntegrationIdAndGuid(integrationId, guid)) continue;
                posts.save(BlogPost.builder().user(integration.getUser()).integration(integration)
                        .guid(guid).title(truncate(title, 300))
                        .description(firstText(item, "description", "summary", "content"))
                        .url(truncate(link, 500))
                        .publishedAt(parseDate(firstText(item, "pubDate", "published", "updated"))).build());
            }
            integration.markSynced(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("RSS 수집에 실패했습니다.", e);
        }
    }

    private static URI parseHttpsUri(String rssUrl) {
        if (rssUrl == null || rssUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RSS 주소가 필요합니다.");
        }
        URI feed;
        try {
            feed = URI.create(rssUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RSS 주소 형식이 올바르지 않습니다.");
        }
        if (!"https".equalsIgnoreCase(feed.getScheme()) || feed.getHost() == null || feed.getHost().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HTTPS RSS 주소만 사용할 수 있습니다.");
        }
        if (feed.getUserInfo() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증 정보가 포함된 RSS 주소는 사용할 수 없습니다.");
        }
        return feed;
    }

    private static void assertPublicHost(String host) {
        String normalized = host.toLowerCase();
        if ("localhost".equals(normalized)
                || normalized.endsWith(".localhost")
                || normalized.endsWith(".local")
                || "metadata.google.internal".equals(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용하지 않는 RSS 호스트입니다.");
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RSS 호스트를 확인할 수 없습니다.");
        }
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isMulticastAddress()
                    || isUniqueLocalIpv6(address)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사설·내부망 RSS 주소는 사용할 수 없습니다.");
            }
        }
    }

    private static boolean isUniqueLocalIpv6(InetAddress address) {
        byte[] raw = address.getAddress();
        return raw.length == 16 && (raw[0] & 0xfe) == 0xfc;
    }

    private String childText(Element parent, String tag) {
        NodeList matches = parent.getElementsByTagName(tag);
        return matches.getLength() > 0 ? matches.item(0).getTextContent().trim() : null;
    }

    private String firstText(Element parent, String... tags) {
        for (String tag : tags) {
            String text = childText(parent, tag);
            if (text != null && !text.isBlank()) return text;
        }
        return null;
    }

    private String linkOf(Element item) {
        String text = childText(item, "link");
        if (text != null && text.startsWith("https://")) return text;
        NodeList links = item.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            if (!(links.item(i) instanceof Element link)) continue;
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("https://")) return href;
        }
        return text;
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    private LocalDateTime parseDate(String value) {
        if (value == null) return null;
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        } catch (Exception ignored) {
            /* Atom은 ISO-8601 */
        }
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        } catch (Exception ignored) {
            /* Instant */
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneId.of("Asia/Seoul"));
        } catch (Exception ignored) {
            return null;
        }
    }
}
