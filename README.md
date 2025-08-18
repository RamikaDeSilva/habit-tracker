# Habit Tracker

A simple habit tracker app built in Java.  
Start with a console UI and JSON persistence, then grow toward JavaFX or REST.

---

## 📋 Project Plan

### Scope (MVP)
- Console-based app (upgrade to JavaFX later if desired)
- Create, read, update, delete (CRUD) habits
- Each habit has:
    - Title (required)
    - Priority (HIGH / MEDIUM / LOW)
    - Status (TODO / IN PROGRESS / COMPLETE)
- Daily completions tracked
- Each day’s completion percentage logged
- Line chart of daily completion % displayed to the user
- Data persists to JSON file

---

## ✅ User Stories

**US1 — Create habit**  
As a user, I can create a habit with a required title and priority.  
*Acceptance:* Empty title → error. Otherwise, habit appears in All with status TODO.

**US2 — Delete habit**  
As a user, I can delete a habit by id.  
*Acceptance:* Habit no longer appears in any list and is removed from storage.

**US3 — Update habit**  
As a user, I can edit a habit’s name or priority.  
*Acceptance:* Updates are saved and visible after restart.

**US4 — Mark habit complete for today**  
As a user, I can mark a habit as completed for the current date.  
*Acceptance:* Habit is shown as DONE for that day and counted toward daily %.

**US5 — View all habits**  
As a user, I can list all habits, showing their priority and most recent status.  
*Acceptance:* List is sorted by priority (HIGH → LOW).

**US6 — Daily completion percentage**  
As a user, I can see the percentage of habits completed for the current day.  
*Acceptance:* % = (# habits completed / total active habits) * 100.

**US7 — Completion history**  
As a user, I can view historical percentages per day.  
*Acceptance:* Stored data reflects the correct % each day.

**US8 — Graph progress**  
As a user, I can see a line chart of daily completion % over time.  
*Acceptance:* Each day is a point; line connects them to show trend.

**US9 — Persistence**  
As a user, my habits and completion history persist across restarts.  
*Acceptance:* Closing and reopening shows the same habits and history (JSON round-trip).

---

## 🗂️ Commands (console)

- `create <name> <priority>` — add a new habit
- `delete <habitId>` — remove a habit
- `update <habitId> <name/priority>` — edit an existing habit
- `list` — show all habits (sorted by priority)
- `complete <habitId>` — mark a habit complete for today
- `today` — show today’s completion %
- `history` — show completion % for previous days
- `graph` — render ASCII graph in console (upgrade later to JavaFX chart)
- `exit` — save to JSON and quit  

