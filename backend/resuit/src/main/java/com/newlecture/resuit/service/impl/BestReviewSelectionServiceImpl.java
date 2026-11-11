package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.repository.BestReviewSelectionRepository;
import com.newlecture.resuit.repository.BestReviewSelectionRepository.SelectionWindow;
import com.newlecture.resuit.service.BestReviewSelectionService;
import com.newlecture.resuit.service.BestReviewSelectionService.Period;
import com.newlecture.resuit.service.BestReviewSelectionService.Result;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BestReviewSelectionServiceImpl implements BestReviewSelectionService {
    private final BestReviewSelectionRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public Result select(Period period, LocalDate start) {
        if (period == null || start == null) throw new IllegalArgumentException("Period and start are required");
        boolean weekly = period == Period.WEEKLY;
        if (weekly ? start.getDayOfWeek() != DayOfWeek.MONDAY : start.getDayOfMonth() != 1) {
            throw new IllegalArgumentException("Period start must be Monday or the first day of the month");
        }
        var end = weekly ? start.plusWeeks(1) : start.plusMonths(1);
        if (end.atStartOfDay(ZONE).toInstant().isAfter(clock.instant())) {
            throw new IllegalArgumentException("Cannot select an unfinished period");
        }
        String type = weekly ? "weekly" : "monthly";
        if (!repository.claim(type, start)) return new Result(period, start, 0, true);
        if (repository.hasResults(type, start)) return new Result(period, start, 0, true);
        int count = repository.select(new SelectionWindow(type, start, start.atStartOfDay(), end.atStartOfDay()));
        return new Result(period, start, count, false);
    }
}
