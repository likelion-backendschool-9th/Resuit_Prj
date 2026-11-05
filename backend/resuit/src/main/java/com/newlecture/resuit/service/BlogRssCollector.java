package com.newlecture.resuit.service;

import com.newlecture.resuit.entity.BlogPost;
import com.newlecture.resuit.entity.Integration;
import com.newlecture.resuit.repository.BlogPostRepository;
import com.newlecture.resuit.repository.IntegrationRepository;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class BlogRssCollector {
    private final IntegrationRepository integrations;
    private final BlogPostRepository posts;
    private final RestClient rest;

    public BlogRssCollector(IntegrationRepository integrations, BlogPostRepository posts) {
        this.integrations = integrations;
        this.posts = posts;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.rest = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Transactional
    public void collect(Long integrationId) {
        Integration integration = integrations.findById(integrationId).orElseThrow();
        if (!integration.isConnected() || !"BLOG".equals(integration.getProvider())) return;
        URI feed = URI.create(integration.getRssUrl());
        String host = feed.getHost();
        if (!"https".equals(feed.getScheme()) || host == null
                || !(host.equals("v2.velog.io") || host.endsWith(".tistory.com"))) {
            throw new IllegalStateException("허용하지 않는 RSS 주소입니다.");
        }
        String xml = rest.get().uri(integration.getRssUrl()).retrieve().body(String.class);
        if (xml == null || xml.length() > 1_000_000) {
            throw new IllegalStateException("RSS 응답 크기가 올바르지 않습니다.");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            NodeList items = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml))).getElementsByTagName("item");
            for (int i = 0; i < Math.min(items.getLength(), 50); i++) {
                Element item = (Element) items.item(i);
                String title = childText(item, "title");
                String link = childText(item, "link");
                String guid = childText(item, "guid");
                if (guid == null || guid.isBlank()) guid = link;
                if (title == null || link == null || guid == null
                        || !link.startsWith("https://")) continue;
                guid = truncate(guid, 500);
                if (posts.existsByIntegrationIdAndGuid(integrationId, guid)) continue;
                posts.save(BlogPost.builder().user(integration.getUser()).integration(integration)
                        .guid(guid).title(truncate(title, 300))
                        .description(childText(item, "description"))
                        .url(truncate(link, 500))
                        .publishedAt(parseDate(childText(item, "pubDate"))).build());
            }
            integration.markSynced(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        } catch (Exception e) {
            throw new IllegalStateException("RSS 수집에 실패했습니다.", e);
        }
    }

    private String childText(Element parent, String tag) {
        NodeList matches = parent.getElementsByTagName(tag);
        return matches.getLength() > 0 ? matches.item(0).getTextContent().trim() : null;
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private LocalDateTime parseDate(String value) {
        if (value == null) return null;
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        } catch (Exception ignored) {
            return null;
        }
    }
}
