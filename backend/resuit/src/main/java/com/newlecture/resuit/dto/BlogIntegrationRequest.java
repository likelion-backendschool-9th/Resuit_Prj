package com.newlecture.resuit.dto;

/**
 * VELOG/TISTORY는 {@code blogId}로 RSS를 만들고, ETC는 {@code rssUrl}을 그대로 쓴다.
 */
public record BlogIntegrationRequest(String platform, String blogId, String rssUrl) {
}
