# EduPixel School Management System — Backend Project Documentation

## 1. Project Overview

EduPixel School Management System is a school-management backend implemented with:

- Python
- FastAPI
- SQLAlchemy
- Pydantic
- SQLite by default
- A normalized relational data model
- An Excel-independent academic rules engine

The backend was designed from the business logic discovered in the workbook `صنف سوم 2.xlsx`.

**Important architectural rule:** the Excel workbook is a source for discovering and validating business rules. The API does **not** open, calculate, or depend on the `.xlsx` file at runtime.

The backend exposes CRUD APIs for school administration and academic data, plus calculated report endpoints equivalent to the important workbook views.

---

## 2. Runtime Architecture

```text
Android / Web Client
        |
        | HTTP / JSON
        v
FastAPI
        |
        +----------------------+
        |                      |
        v                      v
SQLAlchemy ORM          Result Engine
        |                      |
        v                      |
SQLite / configured DB <-------+
```

The client must treat the API as the source of truth.

The Android application should not reimplement academic-result rules locally unless it is only displaying already-calculated values. Result calculations should come from the backend.

---

## 3. Current Backend Identity

- API title: `EduPixel School API`
- Version: `0.3.0`
- Default database: `sqlite:///./edupixel.db`
- Default development server example:

```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 7878
```

- Health endpoint:

```http
GET /health
```

Response:

```json
{
  "status": "ok",
  "service": "edupixel-school",
  "excel_runtime_dependency": "none"
}
```

The backend currently enables permissive CORS:

```text
allow_origins = ["*"]
allow_credentials = false
allow_methods = ["*"]
allow_headers = ["*"]
```

There is currently no authentication or authorization layer.

---

# 4. Data Model

## 4.1 School

Represents a school.

Fields:

```json
{
  "id": 1,
  "name": "Example School",
  "province": "Herat",
  "district": "District 1",
  "manager_name": "Manager",
  "principal_name": "Principal",
  "academic_year_solar": 1404,
  "academic_year_qamari": 1447
}
```

---

## 4.2 Academic Year

Represents an academic year belonging to a school.

Fields:

```json
{
  "id": 1,
  "school_id": 1,
  "solar_year": 1404,
  "qamari_year": 1447,
  "label": "1404-1447",
  "is_active": true
}
```

A school cannot have duplicate `solar_year` records according to the database constraint.

---

## 4.3 Class

Represents a school class/section.

Fields:

```json
{
  "id": 1,
  "school_id": 1,
  "academic_year_id": 1,
  "name": "Third Grade 2",
  "grade_level": "3",
  "section": "2",
  "teacher_name": "Teacher Name",
  "max_absence": 56,
  "policy": {},
  "notes": null
}
```

`max_absence` defaults to `56`.

The class can also contain a JSON `policy` object that overrides result-engine settings.

---

## 4.4 Subject

Subjects are dynamic. The backend is **not hard-coded to a fixed number of subjects**.

Fields:

```json
{
  "id": 1,
  "class_id": 1,
  "name": "Mathematics",
  "order_index": 1,
  "active": true,
  "include_in_results": true,
  "mid_max": 40,
  "annual_max": 60,
  "components_schema": {}
}
```

The result engine only includes subjects that are:

```text
active = true
include_in_results = true
```

Subjects are sorted by:

```text
order_index
then id
```

---

## 4.5 Student

Fields:

```json
{
  "id": 1,
  "class_id": 1,
  "attendance_no": 1,
  "name": "Student Name",
  "father": "Father Name",
  "grandfather": "Grandfather Name",
  "base_number": "123",
  "tazkira": "Tazkira Number",
  "mid_status": null,
  "annual_status": null,
  "notes": null
}
```

`attendance_no` is unique within a class when present.

---

## 4.6 Score

A score belongs to one student and one subject.

```json
{
  "id": 1,
  "student_id": 1,
  "subject_id": 1,
  "mid": 32,
  "annual": 58,
  "mid_components": {},
  "annual_components": {},
  "note": null
}
```

The database enforces one score record per:

```text
student_id + subject_id
```

The `POST /scores` endpoint therefore behaves as an upsert.

---

## 4.7 Attendance

Attendance is stored separately for:

```text
midterm
annual
```

Example:

```json
{
  "id": 1,
  "student_id": 1,
  "period": "midterm",
  "school_days": 81,
  "present": 78,
  "absent": 1,
  "sick": 1,
  "leave": 1,
  "note": null
}
```

There can be one attendance record per student and period.

---

## 4.8 Approval

Used for signatures/review/approval information.

```json
{
  "id": 1,
  "class_id": 1,
  "role": "Reviewer",
  "person_name": "Person",
  "note": null,
  "approved": true,
  "class_type": null
}
```

---

## 4.9 Book Distribution

Tracks textbook/material distribution.

```json
{
  "id": 1,
  "class_id": 1,
  "student_id": 1,
  "subject_id": 1,
  "quantity": 1,
  "delivered": true,
  "signed": true,
  "notes": null
}
```

Database uniqueness:

```text
student_id + subject_id
```

---

# 5. Complete API Surface

## Health

```http
GET /health
```

---

# 6. School CRUD

## Create

```http
POST /schools
```

Body:

```json
{
  "name": "Example School",
  "province": "Herat",
  "district": "District 1",
  "manager_name": "Manager",
  "principal_name": "Principal",
  "academic_year_solar": 1404,
  "academic_year_qamari": 1447
}
```

## List

```http
GET /schools
```

## Get

```http
GET /schools/{school_id}
```

## Update

```http
PUT /schools/{school_id}
```

## Delete

```http
DELETE /schools/{school_id}
```

---

# 7. Academic Year API

```http
POST /academic-years
GET /schools/{school_id}/academic-years
GET /academic-years/{year_id}
PUT /academic-years/{year_id}
DELETE /academic-years/{year_id}
```

Create body:

```json
{
  "school_id": 1,
  "solar_year": 1404,
  "qamari_year": 1447,
  "label": "1404-1447",
  "is_active": true
}
```

---

# 8. Class API

```http
POST /classes
GET /schools/{school_id}/classes
GET /classes/{class_id}
PUT /classes/{class_id}
DELETE /classes/{class_id}
GET /classes/{class_id}/policy
PUT /classes/{class_id}/policy
```

The class list accepts:

```http
GET /schools/{school_id}/classes?year_id=1
```

---

# 9. Subject API

```http
POST /subjects
GET /classes/{class_id}/subjects
GET /subjects/{subject_id}
PUT /subjects/{subject_id}
DELETE /subjects/{subject_id}
```

Query:

```http
GET /classes/{class_id}/subjects?active_only=true
```

Default `active_only` is `true`.

---

# 10. Student API

```http
POST /students
GET /classes/{class_id}/students
GET /students/{student_id}
PUT /students/{student_id}
DELETE /students/{student_id}
```

Student list supports search:

```http
GET /classes/{class_id}/students?q=Ali
```

---

# 11. Score API

## Single score upsert

```http
POST /scores
```

Example:

```json
{
  "student_id": 1,
  "subject_id": 1,
  "mid": 32,
  "annual": 58,
  "mid_components": {
    "homework": 8,
    "activity": 8,
    "oral": 6,
    "written": 10
  },
  "annual_components": {},
  "note": null
}
```

The backend validates that:

- the student exists
- the subject exists
- both belong to the same class
- mid does not exceed `subject.mid_max`
- annual does not exceed `subject.annual_max`

## Bulk score entry

```http
POST /classes/{class_id}/scores/bulk
```

Body:

```json
{
  "items": [
    {
      "student_id": 1,
      "subject_id": 1,
      "mid": 32,
      "annual": 58
    },
    {
      "student_id": 1,
      "subject_id": 2,
      "mid": 30,
      "annual": 55
    }
  ]
}
```

## Read scores

```http
GET /students/{student_id}/scores
GET /scores/{score_id}
GET /scores/{score_id}/components
```

## Update/delete

```http
PUT /scores/{score_id}
DELETE /scores/{score_id}
```

---

# 12. Attendance API

## Upsert

```http
POST /attendance?student_id=1
```

Body:

```json
{
  "period": "midterm",
  "school_days": 81,
  "present": 78,
  "absent": 1,
  "sick": 1,
  "leave": 1,
  "note": null
}
```

`period` must be:

```text
midterm
annual
```

## Bulk

```http
POST /classes/{class_id}/attendance/bulk
```

Example:

```json
{
  "items": [
    {
      "student_id": 1,
      "period": "midterm",
      "school_days": 81,
      "present": 78,
      "absent": 1,
      "sick": 1,
      "leave": 1
    }
  ]
}
```

## Read/update/delete

```http
GET /students/{student_id}/attendance
PUT /attendance/{attendance_id}
DELETE /attendance/{attendance_id}
```

---

# 13. Result Engine

The result engine is located in:

```text
app/engine.py
```

It is independent of Excel.

## Default policy

```json
{
  "max_absence_days": 56,
  "mid_min_subject": 16,
  "mid_avg_pass": 20,
  "annual_min_subject": 40,
  "annual_avg_pass": 50,
  "annual_repeat_below_count": 4,
  "mid_max_score": 40,
  "annual_max_score": 60,
  "final_max_score": 100,
  "mid_excused_code": "1",
  "annual_excused_code": "2",
  "three_part_code": "3"
}
```

---

# 14. Midterm / Four-and-a-Half-Month Result

The backend already calculates the midterm result.

The relevant engine functions are:

```python
mid_result(...)
mid_grade(...)
```

## Midterm result statuses

```text
موفق
تلاش بیشتر
معذرتي
غایب
```

## Midterm rules

### Excused

If:

```text
mid_status == "1"
```

then:

```text
معذرتي
```

### No entered scores

If no valid midterm score exists:

- if school days > 0 → `غایب`
- if school days == 0 → empty result

### Need more effort

If:

```text
average < 20
```

OR:

```text
any subject < 16
```

then:

```text
تلاش بیشتر
```

Otherwise:

```text
موفق
```

---

# 15. Midterm Grades

Grades are calculated from the entered midterm subject scores.

Rules:

```text
no scores             -> ""
minimum < 16          -> هـ
average < 20          -> هـ
average < 24          -> د
average < 30          -> ج
average < 36          -> ب
average <= 40         -> الف
above 40              -> اشتباه
```

The average is calculated over entered numeric midterm scores.

---

# 16. Annual Result

The annual result uses the calculated final scores.

Final score:

```text
final = mid + annual
```

with an important rule:

```text
annual is blank -> final is blank
mid is blank    -> mid is treated as 0 when annual exists
```

Annual statuses:

```text
ارتقا صنف
تکرار صنف
مشروط
معذرتی
محروم
سه پارچه
```

Decision order:

1. Absence threshold
2. Annual special status
3. No final scores
4. Annual average
5. Number of subjects below minimum
6. Promotion/conditional logic

Default annual minimum subject score:

```text
40
```

Default annual average:

```text
50
```

Default absence threshold:

```text
56
```

---

# 17. Annual Grade

Rules:

```text
تکرار صنف -> هـ

محروم / معذرتی / سه پارچه / blank -> ""

any final < 40 or average < 50 -> هـ

average < 60 -> د
average < 75 -> ج
average < 90 -> ب
average <= 100 -> الف
above 100 -> اشتباه
```

---

# 18. Full Student Result Endpoint

```http
GET /students/{student_id}/result
```

This is the primary endpoint for a student's complete calculated result.

Actual logical structure:

```json
{
  "mid_result": "موفق",
  "mid_grade": "ب",

  "annual_result": "ارتقا صنف",
  "annual_grade": "ب",

  "mid_sum": 285,
  "mid_average": 28.5,

  "final_sum": 745,
  "final_average": 74.5,

  "below_40_count": 0,

  "subject_count": 10,
  "entered_mid_subject_count": 10,
  "entered_annual_subject_count": 10,

  "subject_results": [
    {
      "subject_id": 1,
      "subject_name": "Mathematics",
      "order_index": 1,
      "mid": 32,
      "annual": 58,
      "final": 90,
      "mid_components": {},
      "annual_components": {},
      "mid_component_total": null,
      "annual_component_total": null
    }
  ],

  "attendance": {
    "mid": {
      "school_days": 81,
      "present": 78,
      "absent": 1,
      "sick": 1,
      "leave": 1,
      "note": null
    },
    "annual": {
      "school_days": 185,
      "present": 180,
      "absent": 5,
      "sick": 0,
      "leave": 0,
      "note": null
    }
  },

  "student_id": 1,
  "student_name": "Student Name",
  "attendance_no": 1,

  "message": "..."
}
```

This endpoint is ideal for an Android student-detail/report-card screen.

---

# 19. Full Class Results Endpoint

```http
GET /classes/{class_id}/results
```

Response:

```json
{
  "class_id": 1,
  "students": [
    {
      "mid_result": "موفق",
      "mid_grade": "ب",
      "annual_result": "ارتقا صنف",
      "annual_grade": "ب",
      "mid_sum": 285,
      "mid_average": 28.5,
      "final_sum": 745,
      "final_average": 74.5,
      "below_40_count": 0,
      "subject_count": 10,
      "entered_mid_subject_count": 10,
      "entered_annual_subject_count": 10,
      "subject_results": [],
      "attendance": {
        "mid": {},
        "annual": {}
      },
      "student_id": 1,
      "student_name": "Student",
      "attendance_no": 1,
      "message": "..."
    }
  ],
  "summary": {
    "total_enrolled": 30,
    "mid_exam_included": 30,
    "annual_exam_included": 30,
    "mid": {
      "موفق": 24,
      "تلاش بیشتر": 4,
      "معذرتي": 1,
      "غایب": 1
    },
    "annual": {
      "ارتقا صنف": 20,
      "تکرار صنف": 5,
      "مشروط": 2,
      "معذرتی": 1,
      "محروم": 1,
      "سه پارچه": 1
    }
  }
}
```

The numeric values above are examples. The structure is the important contract.

---

# 20. Important: There Is No Dedicated `/midterm-results` Endpoint

The backend does **not** currently define:

```http
GET /classes/{class_id}/midterm-results
```

or:

```http
GET /students/{student_id}/midterm-result
```

Instead, the existing general result endpoints contain the midterm data:

```text
GET /students/{id}/result
GET /classes/{id}/results
```

Those results contain:

```text
mid_result
mid_grade
mid_sum
mid_average
entered_mid_subject_count
subject_results[].mid
attendance.mid
```

Therefore the Android client can already render a complete midterm view without a new endpoint.

---

# 21. Compact Register

```http
GET /classes/{class_id}/compact-register
```

Returns one compact row per student.

Fields include:

```json
{
  "attendance_no": 1,
  "student_id": 1,
  "name": "Student",
  "father": "Father",
  "grandfather": "Grandfather",
  "tazkira": "...",
  "base_number": "...",
  "mid_average": 28.5,
  "annual_average": 74.5,
  "mid_grade": "ب",
  "annual_grade": "ب",
  "annual_result": "ارتقا صنف",
  "annual_absent": 5
}
```

---

# 22. Subject Register

```http
GET /classes/{class_id}/subject-register
```

This endpoint is designed for dynamic subject columns.

Response contains:

```json
{
  "class_id": 1,
  "subjects": [],
  "rows": []
}
```

Each row contains:

```text
attendance_no
student_id
student_name
subjects
final_sum
final_average
annual_result
annual_grade
attendance
```

The `subjects` object uses subject IDs as keys.

---

# 23. Notice Cards

```http
GET /classes/{class_id}/notice-cards
```

Returns one generated notice/report object per student.

Each card contains:

```json
{
  "school": {},
  "class": {},
  "student": {},
  "result": {},
  "message": "..."
}
```

---

# 24. Individual Report Card

```http
GET /students/{student_id}/report-card
```

Response:

```json
{
  "school": {},
  "class": {},
  "student": {},
  "result": {},
  "message": "...",
  "approvals": []
}
```

This is the strongest endpoint for an Android report-card screen because it combines:

- school information
- class information
- student information
- calculated results
- message
- approvals

---

# 25. Class Dashboard

```http
GET /classes/{class_id}/dashboard
```

Returns:

```json
{
  "class_id": 1,
  "students": 30,
  "subjects": 10,
  "summary": {},
  "books": {
    "rows": 300,
    "delivered": 280,
    "signed": 250,
    "quantity": 300
  },
  "approvals": {
    "total": 3,
    "approved": 2
  }
}
```

---

# 26. Other Report Endpoints

```http
GET /classes/{id}/passing-list
GET /classes/{id}/conditional-list
GET /classes/{id}/failed-or-banned-list
GET /classes/{id}/recent-summary
GET /classes/{id}/approval-summary
GET /classes/{id}/cover
GET /classes/{id}/book-distribution-register
```

These correspond to workbook-derived views.

---

# 27. Book Distribution API

```http
POST /book-distributions
GET /classes/{class_id}/book-distributions
PUT /book-distributions/{distribution_id}
DELETE /book-distributions/{distribution_id}
```

The class book register:

```http
GET /classes/{class_id}/book-distribution-register
```

returns dynamic subject columns and student rows.

---

# 28. Shaqah / Component Score Form

```http
GET /students/{student_id}/shaqah
```

This endpoint exposes score-component information for the workbook-style `شقه` form.

Score components are arbitrary JSON objects.

Example:

```json
{
  "homework": 8,
  "activity": 8,
  "oral": 6,
  "written": 10
}
```

The engine can calculate the numeric component total.

---

# 29. Dynamic Subject Architecture

Do not assume:

- 13 subjects
- a fixed subject list
- fixed subject names
- fixed order

The backend deliberately supports any number of subjects.

The Android UI must therefore generate subject lists dynamically from:

```http
GET /classes/{class_id}/subjects
```

or from the result payload.

---

# 30. Error Handling

Typical HTTP behavior:

```text
200 OK
201 Created
400 Bad Request
404 Not Found
409 Conflict
422 Validation Error
```

FastAPI/Pydantic validation errors may return HTTP 422.

Duplicate database relationships can result in HTTP 409.

---

# 31. Current Security State

The current backend has:

- no login
- no JWT
- no refresh tokens
- no user accounts
- no roles/permissions
- no audit log
- open CORS

Therefore the first Android client should be treated as a trusted administrative client or local-network client.

Authentication can be added later without changing the academic data model.

---

# 32. Current Validation Limitations

The backend currently validates:

- nonnegative score values
- score maximums
- student/subject class relationship
- nonnegative attendance counters
- valid attendance period
- required basic relationships

However:

### Attendance total is not enforced

The API does not currently require:

```text
present + absent + sick + leave == school_days
```

The counters are stored independently.

### Component schema is not strictly enforced

`components_schema` describes intended component structure, but the backend does not fully validate every component against the schema or guarantee that component totals equal the official score.

### No migrations

Database creation currently uses:

```python
Base.metadata.create_all(...)
```

There is no Alembic migration system.

### No persistent audit log

There is no backend database table tracking:

```text
who changed what
when
old value
new value
```

---

# 33. Android Client Design Requirements

The Android application should be a native modern Android application.

Recommended architecture:

```text
Kotlin
Jetpack Compose
Material 3 / latest stable Material design libraries
Navigation Compose
ViewModel
Kotlin Coroutines
StateFlow
Retrofit or Ktor Client
Kotlin Serialization or Moshi
Room only for optional local cache
Coil for images if needed
```

The app must communicate with the backend using JSON over HTTP.

---

# 34. Android API Base URL

The base URL must be defined **inside the Android application source/configuration**, not requested from the user during normal operation.

Example:

```kotlin
const val API_BASE_URL = "http://127.0.0.1:7878/"
```

The exact deployed address can be changed at build time.

The app should support a single centralized API configuration:

```text
ApiConfig
```

No screen should hard-code endpoint URLs.

All repositories should use the same API client.

---

# 35. Android UI Language

The Android application UI must be:

```text
English
```

This includes:

- navigation
- buttons
- dialogs
- validation messages
- empty states
- dashboard labels
- settings
- errors
- loading states

The API may return Persian/Afghan result values such as:

```text
موفق
تلاش بیشتر
ارتقا صنف
مشروط
```

The app should display these values faithfully and may optionally provide a presentation mapping layer for English labels.

Do not mutate the backend value.

---

# 36. Visual Direction

Use a premium modern:

```text
Material 3
+
Glass / Liquid Glass inspired visual language
```

Do not use old Material 2 patterns.

Visual characteristics:

- translucent surfaces
- layered cards
- subtle blur where supported
- soft elevation
- large rounded corners
- expressive typography
- modern iconography
- restrained gradients
- dynamic spacing
- edge-to-edge layouts
- adaptive layouts
- excellent dark mode
- responsive tablet/phone layouts
- subtle motion
- clear hierarchy

The design must remain usable and accessible. Glass effects must never reduce text readability.

---

# 37. Main Android Screens

The client should provide at least:

## Dashboard

Display:

- school
- active academic year
- class count
- student count
- subject count
- result summary
- book-distribution summary
- approvals

## Schools

Full CRUD.

## Academic Years

Create/read/update/delete.

## Classes

Full CRUD plus:

- class policy
- teacher
- grade
- section
- students
- subjects
- dashboard

## Subjects

Full CRUD.

Dynamic order.

Configurable midterm/annual maximums.

## Students

Full CRUD.

Search.

Student detail.

## Scores

Enter/edit:

- midterm
- annual
- components

Support both:

- individual entry
- bulk class entry

## Attendance

Separate tabs:

- Midterm
- Annual

Support:

- school days
- present
- absent
- sick
- leave
- notes

## Midterm Results

This is a major screen.

Use:

```http
GET /classes/{class_id}/results
```

and render:

- all students
- all subjects
- midterm scores
- midterm sum
- midterm average
- midterm grade
- midterm result
- midterm attendance
- entered-subject count

Do not wait for annual scores to exist.

## Annual Results

Display:

- annual scores
- final scores
- final average
- annual grade
- annual result
- absence status

## Student Report Card

Use:

```http
GET /students/{student_id}/report-card
```

## Subject Register

Use:

```http
GET /classes/{class_id}/subject-register
```

## Compact Register

Use:

```http
GET /classes/{class_id}/compact-register
```

## Notice Cards

Use:

```http
GET /classes/{class_id}/notice-cards
```

## Passing / Conditional / Failed / Banned

Provide separate filtered views.

## Book Distribution

Full CRUD and register.

## Approvals

View and edit approval records.

---

# 38. Loading and Error UX

Every API screen must have:

- skeleton/loading state
- empty state
- retry state
- network error state
- validation error state
- success confirmation
- destructive-action confirmation

Never leave a blank screen when an API request fails.

---

# 39. Offline/Network Considerations

The first version does not need to be fully offline-first.

However:

- cache the last successful dashboard/result data where practical
- show a clear offline state
- never silently overwrite local edits
- prevent duplicate submissions while a request is pending
- disable submit buttons during active mutations
- refresh data after successful create/update/delete operations

---

# 40. Important Result-Rendering Rule

The Android client must distinguish:

```text
Midterm result
```

from:

```text
Annual result
```

A student can have:

```text
mid_result = "موفق"
mid_grade = "ب"
```

while:

```text
annual_result = "..."
annual_grade = "..."
```

because annual data may not yet exist.

Do not visually present a provisional annual calculation as the student's final academic outcome.

---

# 41. Backend Truth

The following should be considered backend-owned:

- grade calculations
- pass/fail logic
- promotion logic
- conditional logic
- absence-based banning
- special statuses
- subject ordering
- result averages
- result summaries

The Android client is a presentation and data-entry client.

---

# 42. Testing Requirements for the Android Client

The agent creating the Android application must test:

1. API health
2. school CRUD
3. academic year CRUD
4. class CRUD
5. subject CRUD
6. student CRUD
7. score creation
8. score update
9. bulk scores
10. attendance creation
11. attendance update
12. bulk attendance
13. midterm result rendering
14. annual result rendering
15. report-card rendering
16. dashboard rendering
17. book distribution
18. approvals
19. network failure
20. empty datasets
21. validation failures
22. duplicate submissions

---

# 43. Backend Test Status

The project includes:

```text
tests/test_api.py
tests/test_engine.py
```

The tests cover:

- CRUD
- dynamic subjects
- score validation
- attendance
- result statuses
- class reports
- dashboard
- book distribution
- engine thresholds
- component totals
- custom policies
- messages

---

# 44. Key Files

```text
app/main.py
```

FastAPI routes and application logic.

```text
app/models.py
```

SQLAlchemy database models.

```text
app/schemas.py
```

Pydantic API contracts.

```text
app/engine.py
```

Academic/result calculation engine.

```text
tests/test_api.py
```

API integration tests.

```text
tests/test_engine.py
```

Result-engine tests.

```text
EXCEL_AUDIT.md
```

Workbook-to-backend rule mapping.

---

# 45. Core Principle for the Android Agent

Do not invent API endpoints that do not exist.

Before implementing a screen, map it to an existing endpoint.

If a required UI feature cannot be implemented cleanly with the existing API, document the missing capability instead of silently fabricating a response format.

The most important existing endpoints are:

```text
GET /health

GET /schools
GET /schools/{id}/academic-years
GET /schools/{id}/classes

GET /classes/{id}/subjects
GET /classes/{id}/students

GET /students/{id}/scores
GET /students/{id}/attendance
GET /students/{id}/result
GET /students/{id}/report-card
GET /students/{id}/shaqah

GET /classes/{id}/results
GET /classes/{id}/dashboard
GET /classes/{id}/subject-register
GET /classes/{id}/compact-register
GET /classes/{id}/notice-cards
GET /classes/{id}/passing-list
GET /classes/{id}/conditional-list
GET /classes/{id}/failed-or-banned-list
GET /classes/{id}/recent-summary
GET /classes/{id}/approval-summary
GET /classes/{id}/cover
GET /classes/{id}/book-distribution-register
```

---

# 46. Final Project Summary

EduPixel is currently a normalized, Excel-independent school-management backend with:

- dynamic schools
- academic years
- classes
- subjects
- students
- scores
- score components
- midterm calculations
- annual calculations
- attendance
- result statuses
- grades
- report cards
- notice cards
- class reports
- dashboards
- approvals
- book distribution
- bulk data entry
- configurable class result policies

The Android client should consume this API directly and provide a polished, modern administrative experience without duplicating the academic calculation engine.
