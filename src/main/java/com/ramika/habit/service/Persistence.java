package com.ramika.habit.service;

import com.google.gson.*;
import com.ramika.habit.model.Category;
import com.ramika.habit.model.Habit;
import com.ramika.habit.model.Priority;
import com.ramika.habit.model.Status;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * JSON persistence for:
 *  - Habits (id, name, priority, category, status, schedule)
 *  - Per-habit completions: ISO dates ("YYYY-MM-DD")
 *
 * File saved at ~/.habit-hero.json
 */
public final class Persistence {
    private Persistence() {}

    private static final Path DEFAULT_FILE =
            Paths.get(System.getProperty("user.home"), ".habit-hero.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ===== Public API =====

    /** Load habits + completions from disk into HabitService. */
    public static void bootstrapLoad() {
        AppState st = loadFromDisk();
        if (st == null) return;

        Map<UUID, Habit> created = new HashMap<>();
        if (st.habits != null) {
            for (HabitRec r : st.habits) {
                UUID id = r.id != null ? r.id : UUID.randomUUID();
                EnumSet<DayOfWeek> sched = EnumSet.noneOf(DayOfWeek.class);
                if (r.schedule != null) {
                    for (String d : r.schedule) {
                        try { sched.add(DayOfWeek.valueOf(d)); } catch (Throwable ignored) {}
                    }
                }

                HabitService.createHabitFromPersistence(
                        id,
                        r.name,
                        safeEnum(r.priority, Priority.class, Priority.LOW),
                        safeEnum(r.category, Category.class, Category.OTHER),
                        sched,
                        safeEnum(r.status, Status.class, Status.ACTIVE)
                );

                Habit h = HabitService.getAllHabits().get(id);
                if (h != null) created.put(id, h);
            }
        }

        // Apply completions
        if (st.habits != null) {
            for (HabitRec r : st.habits) {
                if (r.completions == null || r.id == null) continue;
                Habit h = created.get(r.id);
                if (h == null) continue;
                for (String ds : r.completions) {
                    try {
                        LocalDate d = LocalDate.parse(ds);
                        h.setCompletedOn(d, true);
                    } catch (Throwable ignored) {}
                }
            }
        }

        HabitService.forceRecompute();
    }

    /** Save all habits + completions to disk. */
    public static void saveSnapshot() {
        AppState st = new AppState();
        st.habits = new ArrayList<>();

        for (Habit h : HabitService.getAllHabits().values()) {
            HabitRec r = new HabitRec();
            r.id = h.getId();
            r.name = h.getName();
            r.priority = safeName(h.getPriority());
            r.category = safeName(h.getCategory());
            r.status = safeName(h.getActiveStatus());

            r.schedule = new ArrayList<>();
            if (h.getSchedule() != null) {
                for (DayOfWeek d : h.getSchedule()) r.schedule.add(d.name());
            }

            r.completions = new ArrayList<>();
            if (h.getCompletions() != null) {
                h.getCompletions().forEach(hc -> {
                    try {
                        if (hc.isCompleted()) r.completions.add(hc.getDate().toString());
                    } catch (Throwable ignored) {}
                });
            }

            st.habits.add(r);
        }

        saveToDisk(st);
    }

    // ===== Disk IO =====

    private static AppState loadFromDisk() {
        try {
            if (!Files.exists(DEFAULT_FILE)) return new AppState();
            try (Reader r = Files.newBufferedReader(DEFAULT_FILE)) {
                AppState st = GSON.fromJson(r, AppState.class);
                return st == null ? new AppState() : st;
            }
        } catch (IOException e) {
            return new AppState(); // fail safe
        }
    }

    private static void saveToDisk(AppState st) {
        try {
            Path parent = DEFAULT_FILE.getParent();
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
            try (Writer w = Files.newBufferedWriter(DEFAULT_FILE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(st, w);
            }
        } catch (IOException ignored) {}
    }

    // ===== DTOs =====

    private static class AppState {
        List<HabitRec> habits = new ArrayList<>();
    }

    private static class HabitRec {
        UUID id;
        String name;
        String priority;
        String category;
        String status;
        List<String> schedule;
        List<String> completions; // ISO dates
    }

    // ===== helpers =====

    private static String safeName(Enum<?> e) { return e == null ? null : e.name(); }

    private static <E extends Enum<E>> E safeEnum(String n, Class<E> t, E def) {
        if (n == null) return def;
        try { return Enum.valueOf(t, n); } catch (Throwable ex) { return def; }
    }
}
