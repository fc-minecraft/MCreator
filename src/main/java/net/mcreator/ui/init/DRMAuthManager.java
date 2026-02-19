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
    private static final File AUTH_FILE = new File(System.getProperty("user.home"), ".mcreator/drm_auth.bin");
    private static final Gson GSON = new GsonBuilder().create();
    private static AuthData currentSession;

    // Obfuscated memory for Secrets
    private static String getHmacSecret() {
        byte[] e = new byte[] { 24, 22, 7, 16, 20, 1, 26, 7, 10, 17, 7, 24, 10, 6, 16, 22, 7, 16, 1, 10, 103, 101, 103,
                97, 10, 29, 20, 7, 17, 16, 27, 16, 17 };
        for (int i = 0; i < e.length; i++)
            e[i] ^= 0x55;
        return new String(e, StandardCharsets.UTF_8);
    }

    private static byte[] getSalt() {
        byte[] e = new byte[] { 24, 22, 39, 48, 52, 33, 58, 39 };
        for (int i = 0; i < e.length; i++)
            e[i] ^= 0x55;
        return e;
    }

    private static String getApiBaseUrl() {
        if (PreferencesManager.PREFERENCES != null && PreferencesManager.PREFERENCES.hidden != null) {
            String url = PreferencesManager.PREFERENCES.hidden.drmApiUrl.get();
            if (url == null || url.isEmpty())
                return "https://api.funcode.school/api/auth";
            return url.endsWith("/") ? url + "api/auth" : url + "/api/auth";
        }
        return "https://api.funcode.school/api/auth";
    }

    // Internal centralized fetch, but explicit external validation
    private static synchronized AuthData getActiveSession() {
        if (currentSession == null)
            loadSession();
        if (currentSession == null)
            return null;
        try {
            Instant now = Instant.now();
            if (currentSession.lastCheckedTime != null) {
                Instant lastChecked = Instant.parse(currentSession.lastCheckedTime);
                if (now.isBefore(lastChecked.minusSeconds(600))) {
                    LOG.error("System time tampering detected!");
                    return null;
                }
            }
            if (now.isAfter(Instant.parse(currentSession.authExpire)))
                return null;
            return currentSession;
        } catch (Exception e) {
            return null;
        }
    }

    public static synchronized boolean hasValidSession() {
        AuthData session = getActiveSession();
        if (session != null) {
            session.lastCheckedTime = Instant.now().toString();
            saveSession();
            return true;
        }
        logout();
        return false;
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
            data.lastCheckedTime = Instant.now().toString();

            currentSession = data;
            saveSession();
            return null;
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
        // Decentralized check #1
        AuthData sess = getActiveSession();
        if (sess == null)
            return 0;
        try {
            Instant expireTime = Instant.parse(sess.authExpire);
            return Math.max(0, ChronoUnit.DAYS.between(Instant.now(), expireTime));
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

    public static void validateOrCrash() {
        // Decentralized check #2: Independent File Validation to avoid single point of
        // failure
        if (!AUTH_FILE.exists())
            throw new SecurityException("Runtime Integrity Check Failed: No Session");
        try {
            byte[] encryptedData = java.nio.file.Files.readAllBytes(AUTH_FILE.toPath());
            String json = decrypt(encryptedData);
            AuthData data = GSON.fromJson(json, AuthData.class);
            if (data.token == null)
                throw new SecurityException("Invalid Token");

            Instant expireTime = Instant.parse(data.authExpire);
            if (Instant.now().isAfter(expireTime)) {
                throw new SecurityException("Session Expired");
            }
        } catch (Exception e) {
            throw new SecurityException("Runtime Integrity Check Failed: " + e.getMessage());
        }
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
            LOG.warn("Failed to load/decrypt auth session (HWID mismatch or tampering)");
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
        StringBuilder hwidBuilder = new StringBuilder();
        try {
            java.util.Enumeration<java.net.NetworkInterface> networkInterfaces = java.net.NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = networkInterfaces.nextElement();
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null) {
                    for (byte b : hardwareAddress)
                        hwidBuilder.append(String.format("%02X", b));
                }
            }
        } catch (Exception e) {
        }

        hwidBuilder.append(System.getProperty("os.name"));
        hwidBuilder.append(System.getProperty("os.arch"));
        hwidBuilder.append(Runtime.getRuntime().availableProcessors());

        String envProcessor = System.getenv("PROCESSOR_IDENTIFIER");
        if (envProcessor != null)
            hwidBuilder.append(envProcessor);

        String envComputer = System.getenv("COMPUTERNAME");
        if (envComputer == null)
            envComputer = System.getenv("HOSTNAME");
        if (envComputer != null)
            hwidBuilder.append(envComputer);

        String hwid = hwidBuilder.toString();

        javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                (hwid + getHmacSecret()).toCharArray(),
                getSalt(),
                65536,
                256);
        return new javax.crypto.spec.SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] encrypt(String data) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getSecretKey(), ivSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return combined;
    }

    private static String decrypt(byte[] combined) throws Exception {
        if (combined.length < 16)
            throw new Exception("Invalid data");
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);

        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getSecretKey(), ivSpec);
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
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
                System.err.println(errorMsg);
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
        String lastCheckedTime;
    }
}
