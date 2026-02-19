# DRM Auth System Specification

## 1. System Overview
The DRM Auth System serves as a mandatory gatekeeper for the MCreator application, ensuring that only authenticated users with a valid session can access the software. It integrates with the `api.itmind.kz` backend for credential validation and session management.

## 2. Authentication Flow

### 2.1 Startup Sequence
1.  **Initialization**: `Launcher.main()` executes before the JavaFX/Swing application starts.
2.  **Session Validation**: `DRMAuthManager.validate()` checks for a locally stored session token.
    *   **Valid Session**: Application launch proceeds.
    *   **Invalid/Expired Session**: `DRMLoginDialog` is displayed (blocking the UI thread).
    *   **Exit**: If the user closes the login dialog without authenticating, `System.exit(0)` is called.

### 2.2 Token Management
*   **Storage Location**: `~/.mcreator/drm_auth.json` (OS-dependent user home directory).
*   **Storage Format**: HMAC-SHA256 Signed JSON.
    ```json
    {
      "data": {
        "token": "...",
        "refresh": "...",
        "login": "...",
        "authExpire": "...",
        "refreshExpire": "..."
      },
      "signature": "hmac_sha256_hash_of_data_json_string"
    }
    ```
*   **Security**: Signature is verified on load. Mismatch results in session invalidation.

### 2.3 API
### 1. Endpoint
**Base URL**: `https://api.funcode.school/api/auth` (Configurable via `userpreferences.json` -> `hidden.drmApiUrl`)

### 2. Authentication Flow
1.  **Client** sends `POST /login` with `{ "login": "user", "password": "password", "role": "student" }`.
Authenticates a user and retrieves session tokens.

**Request:**
```json
{
  "login": "username",
  "password": "password"
}
```

**Response (200 OK):**
```json
{
  "refresh": "refresh_token_string",
  "refreshExpire": "2026-02-19T11:55:34.307Z", // ISO 8601
  "token": "access_token_string",
  "authExpire": "2026-02-19T11:55:34.307Z"    // ISO 8601
}
```

## 3. User Interface Specifications

### 3.1 Login Dialog (`DRMLoginDialog`)
*   **Type**: Modal Swing `JDialog`.
*   **Dimensions**: 500x450 pixels.
*   **Styling**:
    *   **Background**: Dark Blue (`#1E2A3C`).
    *   **Text**: White, Roboto/San-Serif.
    *   **Header**: "ВОЙДИ НА УЧЕБНУЮ ПЛАТФОРМУ, ЧТОБЫ ПРОДОЛЖИТЬ" (Bold, 18pt).
    *   **Inputs**: Large (40px height), padded.
    *   **Button**: Cyan (`#53DDFF`) background, Black text, 200x45px.

### 3.2 Main Menu Integration (`MainMenuBar`)
*   **Component**: `JButton` added to the top-level menu bar.
*   **Position**: Right-aligned (`Box.createHorizontalGlue()`).
*   **Label Format**: "Выйти ({days_remaining} дн.)".
*   **Functionality**:
    *   Clears local `drm_auth.json`.
    *   Invalidates in-memory session.
    *   Restarts application or prompts for immediate re-login.

## 4. Security & Error Handling
*   **Thread Safety**: `DRMAuthManager` methods are synchronized to prevent race conditions during token reads/writes.
*   **Network Timeouts**: Connection timeout set to 5000ms.
*   **Exception Handling**:
    *   Network failures gracefull fail to "Offline" check if session exists.
    *   JSON parsing errors trigger re-authentication.
