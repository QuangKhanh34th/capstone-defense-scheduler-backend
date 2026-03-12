# Mobile Application API Endpoints

## Overview

**Base URL**: `http://localhost:8080/api`
**Timeout**: 30 seconds
**Authentication**: Bearer Token (JWT)

---

## 🔐 Authentication (`auth_api.dart`)

| Method | Endpoint        | Purpose                   | Params/Body                                |
| ------ | --------------- | ------------------------- | ------------------------------------------ |
| `POST` | `/auth/login`   | User login                | `{ "username": "...", "password": "..." }` |
| `POST` | `/auth/refresh` | Refresh access token      | `{ "refreshToken": "..." }`                |
| `POST` | `/auth/logout`  | Logout (invalidate token) | _None_                                     |
| `GET`  | `/v1/users/me`  | Fetch User Profile        | _Headers: Authorization_                   |

---

## 📅 Lecturer Operations (`lecturer_api.dart`)

### Defense Rounds

| Method | Endpoint                    | Purpose                                 |
| ------ | --------------------------- | --------------------------------------- |
| `GET`  | `/v1/rounds`                | Get list of all defense rounds          |
| `GET`  | `/v1/rounds/{roundId}/days` | Get available days for a specific round |

### Availability Management

| Method   | Endpoint                                                             | Purpose                     | Body                                                                   |
| -------- | -------------------------------------------------------------------- | --------------------------- | ---------------------------------------------------------------------- |
| `POST`   | `/v1/availability`                                                   | Register availability       | `{ "userId": "...", "roundId": "...", "availableDate": "yyyy-MM-dd" }` |
| `DELETE` | `/v1/availability/lecturer/{lecturerId}/round/{roundId}/date/{date}` | Unregister availability     | _Path Params_                                                          |
| `GET`    | `/v1/availability/lecturer/{lecturerId}/round/{roundId}`             | Get registered availability | _Path Params_                                                          |

### Schedule & Personal Info

| Method | Endpoint                    | Purpose                       |
| ------ | --------------------------- | ----------------------------- |
| `GET`  | `/v1/lecturers/me/schedule` | Get personal defense schedule |
| `GET`  | `/v1/users/me`              | Get personal profile details  |

---

## 🔄 Data Types & Models

### User

- `username`
- `role`
- `accessToken`
- `refreshToken`
- `lecturerInfo` (contains `lecturerId`)

### DefenseRound

- `id`
- `name`
- `status`

### DefenseDay

- `id`
- `date`
- `roundId`

### DefenseSchedule

- _(Structure depends on API response)_
