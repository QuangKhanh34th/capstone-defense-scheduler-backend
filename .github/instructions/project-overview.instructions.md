# instruction.md

## Role & Objective
You are an expert Backend Software Engineer and System Architect. Your goal is to assist in building the **Minimum Viable Product (MVP)** for the **Thesis Defense Automated Scheduling System (TDASS)**.

Focus strictly on the MVP scope defined below. Do not implement advanced features (like "Compatibility Scores" or complex "Room/Logistics" management) unless explicitly requested.

---

## 1. MVP Scope & Workflow
The MVP focuses on the core "Happy Path": **Setup → Import → Availability → Scheduling → Finalize**.
Follow this exact implementation sequence (based on project specifications):

1.  **Create Defense Round** (`POST /api/v1/rounds`)
2.  **Import Projects** (`POST /api/v1/projects/import`) - *Auto-assign to the created Round.*
3.  **Import Lecturers** (`POST /api/v1/lecturers/import`) - *Excel import including competency/quota.*
4.  **Register Availability** (`POST /api/v1/lecturer/availability`) - *Lecturers input free time.*
5.  **Create Council Blocks** (`POST /api/v1/days/{dayId}/blocks`) - *Auto-distribute imported projects into time blocks.*
6.  **Run Scheduling Algorithm** (`POST /api/v1/rounds/{roundId}/schedule/run`) - *The Core Engine.*
7.  **Get Schedule** (`GET /api/v1/rounds/{roundId}/schedule`)
8.  **Update Assignment** (`PUT /api/v1/blocks/{blockId}/assignments`) - *Manual adjustment.*
9.  **Finalize Round** (`POST /api/v1/rounds/{roundId}/finalize`) - *Lock schedule.*

---

## 2. Core Entities (MVP Schema)
Reference these entities when building Models/DTOs.
*Note: Ignore "Room" or "Equipment" for MVP.*

* **E-03 Defense_rounds:** Parent entity for the scheduling session.
* **E-02 Projects:** Linked to a Round.
* **E-04 Lecturers:** Includes `department_id` and basic profile.
* **E-15 Lecturer_availabilities:** Stores dates/slots a lecturer is free.
* **E-08 Council_blocks:** A time slot (e.g., "Morning Slot 1") containing multiple projects.
* **E-12 Councils:** The committee assigned to a Block.
* **E-12 Council_block_assignments:** Links `Lecturer` to `Council_block` with a specific `Role`.
* **E-11 Project_supervisors:** Critical for conflict detection (Supervisor cannot sit in Council for their own student).

---

## 3. Business Rules (The "Brain")
The Scheduling Algorithm (`API #23`) MUST enforce the following **HARD** constraints. If these are violated, the schedule is invalid.

### Hard Constraints (Must Implement):
* **BR-17 (5 Roles):** Every Council Block must have exactly 5 distinct roles (e.g., President, Secretary, etc.).
* **BR-25 (No Double Booking):** A Lecturer cannot be in two Council Blocks that overlap in time.
* **BR-31 & BR-32 (Supervisor Conflict):** * A Lecturer **cannot** be a Council Member for a block that contains a project they supervise.
    * *Logic:* If Block A has Project X, and Lecturer L supervises Project X -> Lecturer L cannot be assigned to Block A.
* **BR-33 (Availability):** Lecturer must be marked "Available" for the specific slot to be assigned.
* **BR-27 (Quota):** Do not assign a Lecturer more slots than their `max_quota`.

### MVP Simplifications:
* **Project Import:** When importing, automatically assign projects to the selected Round (no need for complex "pending" states).
* **Council Blocks:** For MVP, create blocks on the "Current Date" or a fixed "DefenseDay" immediately upon creation.

---

## 4. API Implementation Details
When generating code for Controllers/Services, adhere to these specific logic notes:

### A. Create Lecturer (Import)
* **Endpoint:** `POST /api/v1/lecturers/import`
* **Logic:** Parse Excel file. Columns: Name, Email, Dept, **Competency**, **Quota**.
* **Note:** Create the User account automatically upon import if it doesn't exist.

### B. Create Council Block
* **Endpoint:** `POST /api/v1/days/{dayId}/blocks`
* **MVP Logic:** 1. Fetch all Projects imported for this Round.
    2. Group them into chunks of **6-7 projects** (BR-13).
    3. Create a `Council_block` for each chunk.
    4. Link projects to these blocks immediately.

### C. Run Scheduling (The Algorithm)
* **Endpoint:** `POST /api/v1/rounds/{roundId}/schedule/run`
* **Strategy:**
    1. Fetch all `Council_blocks` in the Round.
    2. For each Block, find 5 Lecturers who:
        * Are Available (BR-23).
        * Are NOT Supervisors of any project in this block (BR-31).
        * Are NOT already assigned to another block at this time (BR-25).
        * Have not exceeded Quota (BR-27).
    3. Assign specific roles based on `Competency` (greedy approach is fine for MVP).
    4. Save to `Council_block_assignments`.

---

## 5. Coding Standards
* **Error Handling:** Return `400 Bad Request` with specific error messages if a Business Rule (e.g., BR-31) is violated during manual assignment.
* **Transactional:** Scheduling runs must be transactional. If the algorithm fails to fit 1 block, Rollback (or mark as "Unassigned" for manual fix).
* **DTOs:** Always use DTOs for API requests/responses, never expose Entity classes directly.