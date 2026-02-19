package net.mcreator.ui.init;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mcreator.preferences.PreferencesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DRMAuthManager {

    private static final Logger LOG = LogManager.getLogger("DRM");
    // "Fortress" Mode Constants
    private static final String HMAC_SECRET = "MCREATOR_DRM_SECRET_2024_HARDENED";
    private static final byte[] SALT = new byte[] { 0x4D, 0x43, 0x72, 0x65, 0x61, 0x74, 0x6F, 0x72 }; // "MCreator"
    private static final File AUTH_FILE = new File(System.getProperty("user.home"), ".mcreator/drm_auth.bin"); // Binary
                                                                                                               // file
    private static final Gson GSON = new GsonBuilder().create();
    private static AuthData currentSession;

    private static String getApiBaseUrl() {
        if (PreferencesManager.PREFERENCES != null && PreferencesManager.PREFERENCES.hidden != null) {
            String url = PreferencesManager.PREFERENCES.hidden.drmApiUrl.get();
            if (url == null || url.isEmpty())
                return "https://api.funcode.school/api/auth";
            // Append /api/auth if not present, assuming user gives domain like
            // https://api.funcode.school
            return url.endsWith("/") ? url + "api/auth" : url + "/api/auth";
        }
        return "https://api.funcode.school/api/auth"; // Fallback
    }

    public static synchronized boolean validate() {
        if (currentSession == null) {
            loadSession();
        }

        if (currentSession == null) {
            return false;
        }

        // Check expiration
        try {
            // 1. Check if token corrupted/tampered (Format check)
            Instant.parse(currentSession.authExpire);

            Instant now = Instant.now();

            // 2. Rolling Timestamp Check (Anti-Time-Travel)
            if (currentSession.lastCheckedTime != null) {
                Instant lastChecked = Instant.parse(currentSession.lastCheckedTime);
                if (now.isBefore(lastChecked.minusSeconds(600))) { // 10 mins buffer for sync
                    LOG.error("System time tampering detected! Current: " + now + ", Last: " + lastChecked);
                    logout();
                    return false;
                }
            }

            // Check auth expiration
            Instant expireTime = Instant.parse(currentSession.authExpire);
            if (now.isAfter(expireTime)) {
                LOG.info("Session expired.");
                return false;
            }

            // Update rolling timestamp
            currentSession.lastCheckedTime = now.toString();
            saveSession(); // Re-encrypt with new time

            return true;
        } catch (Exception e) {
            LOG.error("Failed to parse expiration date or validate session", e);
            logout(); // Corrupted data
            return false;
        }
    }

    public static synchronized String login(String login, String password) throws IOException {
        String jsonBody = GSON.toJson(new LoginRequest(login, password));
        String response = sendPostRequest(getApiBaseUrl() + "/login", jsonBody);

        if (response != null && response.contains("token")) {
            AuthResponse resp = GSON.fromJson(response, AuthResponse.class);

            AuthData data = new AuthData();
            data.token = resp.token;
            data.refresh = resp.refresh;
            data.login = login;
            data.authExpire = resp.authExpire;
            data.refreshExpire = resp.refreshExpire;
            data.lastCheckedTime = Instant.now().toString(); // Init timestamp

            currentSession = data;
            saveSession();
            return null; // Success
        } else {
            return "Неверный ответ от сервера";
        }
    }

    public static synchronized void logout() {
        currentSession = null;
        if (AUTH_FILE.exists()) {
            AUTH_FILE.delete();
        }
    }

    public static long getDaysRemaining() {
        if (!validate())
            return 0;
        try {
            Instant expireTime = Instant.parse(currentSession.authExpire);
            long days = ChronoUnit.DAYS.between(Instant.now(), expireTime);
            return Math.max(0, days);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getCurrentLogin() {
        if (currentSession != null) {
            return currentSession.login;
        }
        return "Неизвестно";
    }

    /**
     * Anti-Patching: Critical components calls this.
     * If session is invalid/null (bypassed login), it throws exception or crashes
     * JVM.
     */
    public static void validateOrCrash() {
        if (currentSession == null || currentSession.token == null) {
            // Tampering detected.
            // In a real scenario, we might want to be subtle, but for now:
            throw new SecurityException("Runtime Integrity Check Failed: Invalid Session");
        }
        // Additional integrity checks could go here (e.g. check standard hash of known
        // classes)
    }

    // --- ENCRYPTION & STORAGE (HWID) ---

    private static void loadSession() {
        if (!AUTH_FILE.exists())
            return;
        try {
            byte[] encryptedData = java.nio.file.Files.readAllBytes(AUTH_FILE.toPath());
            String decryptedJson = decrypt(encryptedData);
            currentSession = GSON.fromJson(decryptedJson, AuthData.class);
        } catch (Exception e) {
            LOG.warn("Failed to load/decrypt auth session (HWID mismatch or tampering)", e);
            currentSession = null;
        }
    }

    private static void saveSession() {
        if (!AUTH_FILE.getParentFile().exists()) {
            AUTH_FILE.getParentFile().mkdirs();
        }
        try {
            String json = GSON.toJson(currentSession);
            byte[] encryptedData = encrypt(json);
            java.nio.file.Files.write(AUTH_FILE.toPath(), encryptedData);
        } catch (Exception e) {
            LOG.error("Failed to save/encrypt auth session", e);
        }
    }

    private static javax.crypto.SecretKey getSecretKey() throws Exception {
        // HWID Generation: OS + User + Arch + Processors
        String hwid = System.getProperty("os.name") +
                System.getProperty("os.arch") +
                System.getProperty("os.version") +
                Runtime.getRuntime().availableProcessors() +
                System.getenv("PROCESSOR_IDENTIFIER") +
                System.getenv("COMPUTERNAME") +
                System.getProperty("user.name");

        // Hash HWID + Secret
        javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                (hwid + HMAC_SECRET).toCharArray(),
                SALT,
                65536,
                256);
        return new javax.crypto.spec.SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] encrypt(String data) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding"); // ECB for simplicity in
                                                                                              // this context, or CBC
                                                                                              // with Iv
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getSecretKey());
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String decrypt(byte[] data) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getSecretKey());
        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }

    // --- NETWORK ---

    private static String sendPostRequest(String urlStr, String jsonBody) throws IOException {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000); // 5s timeout
        conn.setReadTimeout(5000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                if (code == 403 || code == 401) {
                    throw new IOException("Неверный логин или пароль");
                }

                String errorMsg = "Ошибка авторизации: " + code + " " + response.toString();
                LOG.error(errorMsg);
                System.err.println(errorMsg); // Explicit console output
                throw new IOException("Сервер вернул ошибку " + code + ": " + response.toString());
            } catch (Exception e) {
                if (code == 403 || code == 401) {
                    throw new IOException("Неверный логин или пароль");
                }
                System.err.println("Ошибка авторизации: Сервер вернул ошибку " + code);
                throw new IOException("Сервер вернул ошибку " + code);
            }
        }
    }

    // Data Classes

    @SuppressWarnings("unused")
    private static class LoginRequest {
        String login;
        String password;
        String role;

        public LoginRequest(String login, String password) {
            this.login = login;
            this.password = password;
            this.role = "student"; // Default
        }
    }

    @SuppressWarnings("unused")
    private static class AuthResponse {
        String refresh;
        String refreshExpire;
        String token;
        String authExpire;
    }

    @SuppressWarnings("unused")
    private static class AuthData {
        String token;
        String refresh;
        String login;
        String authExpire;
        String refreshExpire;
        String lastCheckedTime; // For Rolling Timestamp
    }
}
