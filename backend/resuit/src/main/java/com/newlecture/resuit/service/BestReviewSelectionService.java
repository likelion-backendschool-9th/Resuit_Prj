package com.newlecture.resuit.service;

import java.time.LocalDate;
import java.time.ZoneId;
public interface BestReviewSelectionService {
    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    public enum Period { WEEKLY, MONTHLY }
    public record Result(Period period, LocalDate periodStart, int selected, boolean alreadyCompleted) {}
    Result select(Period period, LocalDate start);
}
