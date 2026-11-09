package com.newlecture.resuit.entity;

import java.util.*;

/** Immutable JSON value; never holds a live entity or a shared resume content map. */
public record SubmittedResume(int schemaVersion, String title, String field,
        Map<String, Object> content, List<Question> questions) {
    public SubmittedResume {
        content = copyContent(content);
        questions = List.copyOf(questions);
    }
    public record Question(Long id, String content, Integer charLimit, int orderNo) {}
    public static Map<String, Object> copyContent(Map<String, Object> value) {
        Map<String, Object> result = new LinkedHashMap<>();
        value.forEach((key, item) -> result.put(key, copy(item)));
        return Collections.unmodifiableMap(result);
    }
    private static Object copy(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(String.valueOf(key), copy(item)));
            return Collections.unmodifiableMap(result);
        }
        if (value instanceof List<?> list) return list.stream().map(SubmittedResume::copy).toList();
        return value;
    }
}
