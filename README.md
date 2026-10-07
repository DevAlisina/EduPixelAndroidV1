# EduPixel School Management System — Android Client

A native Android client built with modern **Kotlin**, **Jetpack Compose**, and **Material 3 (Liquid Glass)** for the **EduPixel School Management System**.

The app connects to the official **FastAPI EduPixel School API v0.3.0** backend served at `http://127.0.0.1:7878/`.

---

## Architecture & Tech Stack

- **Platform:** Android (Min SDK 26, Target SDK 34)
- **UI Framework:** Jetpack Compose & Material 3
- **Design Language:** Modern Liquid Glass / Glassmorphism with translucent surfaces, layered depth, and accessible high-contrast typography
- **Architecture:** Clean Architecture (UI & ViewModels → Repositories → Retrofit HTTP Service → FastAPI Backend)
- **State Management:** Kotlin Coroutines & `StateFlow` with explicit `Loading`, `Success`, `Empty`, and `Error` sealed states
- **Networking:** Retrofit 2 + OkHttp with connection logging & cleartext HTTP support for local development
- **Serialization:** Kotlinx Serialization JSON (`ignoreUnknownKeys = true`, `encodeDefaults = true`)
- **Navigation:** Jetpack Navigation Compose with responsive layout support (BottomBar on phones, NavigationRail on tablets and wide screens)
- **CI/CD:** Automated GitHub Actions workflow compiling Debug & Release APKs and publishing releases with downloadable `.apk` files directly to GitHub Releases.

---

## Features & Endpoints Coverage

| Module | Features | Backend Endpoints |
| :--- | :--- | :--- |
| **Dashboard** | Institutional overview, live enrollment counters, midterm pass rate, annual promotion rate, textbook distribution progress, approval counts | `GET /classes/{id}/dashboard`, `GET /health` |
| **Schools** | Create, view, update, delete schools, active school switcher, principal & manager details | `POST/GET/PUT/DELETE /schools` |
| **Academic Years** | Solar and Qamari year configuration, active year indicator, school-scoped years | `POST/GET/PUT/DELETE /academic-years` |
| **Classes** | Class sections, teachers, max absence threshold, custom academic policy rules editor | `POST/GET/PUT/DELETE /classes`, `GET/PUT /classes/{id}/policy` |
| **Subjects** | Dynamic subjects (not hard-coded), order index, customizable `mid_max` and `annual_max` | `POST/GET/PUT/DELETE /subjects` |
| **Students** | Student roster, roll number badges, search by student name/father/tazkira, full CRUD | `POST/GET/PUT/DELETE /students` |
| **Student Detail** | Consolidated student hub: profile, midterm result, annual result, attendance records, official report card | `GET /students/{id}`, `GET /students/{id}/result`, `GET /students/{id}/report-card`, `GET /students/{id}/attendance` |
| **Scores** | Individual score entry with max-mark validation, class-wide bulk score grid entry | `POST /scores`, `POST /classes/{id}/scores/bulk` |
| **Attendance** | Dedicated Midterm and Annual attendance tabs, present/absent/sick/leave counters, bulk class logger | `POST /attendance`, `POST /classes/{id}/attendance/bulk` |
| **Midterm Results** | Four-and-a-half-month midterm results register, pass/need effort/absent indicators, student score breakdown dialog | `GET /classes/{id}/results` |
| **Annual Results** | Final annual outcomes, promotion/repeat/conditional/banned statuses, final marks & grades | `GET /classes/{id}/results` |
| **Reports** | Compact Register, Subject Register with dynamic columns, Notice Cards, Passing List, Conditional List, Failed/Banned List, Class Cover Sheet, Approval Summary | `/compact-register`, `/subject-register`, `/notice-cards`, `/passing-list`, `/conditional-list`, `/failed-or-banned-list`, `/cover`, `/approval-summary` |
| **Textbooks** | Book distribution tracking, student receipt signatures, delivery confirmation, register | `POST/GET/PUT/DELETE /book-distributions`, `GET /classes/{id}/book-distribution-register` |
| **Approvals** | Class signatories, reviewer and administrative approval status toggles | `POST/GET/PUT/DELETE /approvals` |
| **Settings** | Real-time `/health` check probe, dynamic API Base URL configuration (defaults to `http://127.0.0.1:7878/`) | `GET /health` |

---

## Backend Setup (Local Server)

Run the backend FastAPI server on port `7878`:

```bash
# Unzip backend
unzip EduPixelSchool-Python-API-v3-full.zip -d backend
cd backend/EduPixelSchool-API-v2

# Install dependencies
pip install -r requirements.txt

# Start server
python -m uvicorn app.main:app --host 0.0.0.0 --port 7878
```

Verify backend health:

```bash
curl http://127.0.0.1:7878/health
# {"status":"ok","service":"edupixel-school","excel_runtime_dependency":"none"}
```

---

## Automated GitHub Workflow & Release

When pushed to GitHub (`main` branch or tag `v*`):
1. GitHub Actions sets up JDK 21 and the Android SDK.
2. Runs unit test suite (`./gradlew testDebugUnitTest`).
3. Compiles `assembleDebug` and `assembleRelease`.
4. Automatically publishes a new release under the **Releases** tab on GitHub with the compiled APKs attached for instant download.
