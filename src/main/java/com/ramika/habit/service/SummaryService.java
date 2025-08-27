package com.ramika.habit.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

// Class that deals with math calculations for habits and summarizations
public class SummaryService {
    private Map<Long, Map<LocalDate, Boolean>> habitCompletionMap = new HashMap<>();

    public void markHabitCompleted(Long habitId, LocalDate date) {
        habitCompletionMap
                .computeIfAbsent(habitId, k -> new HashMap<>())
                .put(date, true);
    }

    public boolean isHabitCompleted(Long habitId, LocalDate date) {
        return habitCompletionMap
                .getOrDefault(habitId, new HashMap<>())
                .getOrDefault(date, false);
    }
}
