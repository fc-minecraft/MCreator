package net.mcreator.ui.init;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mcreator.preferences.PreferencesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

public class DRMAuthManager {

    private static final Logger LOG = LogManager.getLogger("DRM");
    // "Fortress" Mode Constants
    // Obfuscated secrets (simple Base64 to prevent easy string discovery)
    private static final String HMAC_SECRET = new String(
            java.util.Base64.getDecoder().decode("TUNSRUFUT1JfRFJNX1NFQ1JFVF8yMDI0X0hBUkRFTkVE"),
            StandardCharsets.UTF_8);
    private static final byte[] SALT = java.util.Base64.getDecoder().decode("TUNyZWF0b3I=");
    private static final File AUTH_FILE = new File(System.getProperty("user.home"), ".mcreator/drm_auth.bin");
    private static final Gson GSON = new GsonBuilder().create();
    private static AuthData currentSession;
    private static String cachedHwid;

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
            if (currentSession.authExpire == null || currentSession.token == null) {
                LOG.info("Session corrupted: Missing critical fields (token/expire)");
                return false;
            }

            Instant now = Instant.now();

            // 2. Rolling Timestamp Check (Anti-Time-Travel)
            if (currentSession.lastCheckedTime != null) {
                try {
                    Instant lastChecked = Instant.parse(currentSession.lastCheckedTime);
                    if (now.isBefore(lastChecked.minusSeconds(600))) { // 10 mins buffer for sync
                        LOG.error("System time tampering detected! Current: " + now + ", Last: " + lastChecked);
                        logout();
                        return false;
                    }
                } catch (Exception e) {
                    LOG.info("Last checked time corrupted, resetting...");
                }
            }

            // Check auth expiration
            Instant expireTime = Instant.parse(currentSession.authExpire);
            if (now.isAfter(expireTime)) {
                LOG.info("DRM Session expired (Limit reached).");
                return false;
            }

            // Update rolling timestamp
            currentSession.lastCheckedTime = now.toString();
            saveSession(); // Re-encrypt with new time

            return true;
        } catch (Exception e) {
            LOG.info("Failed to parse expiration date or validate session: {}", e.getMessage());
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

    /**
     * Entry-point validation: called at startup.
     * Shows the login dialog if no valid session is found and blocks until done.
     */
    private static final java.util.concurrent.atomic.AtomicBoolean dialogShowing = new java.util.concurrent.atomic.AtomicBoolean(
            false);

    public static void validateOrCrash() {
        if (!validate()) {
            if (dialogShowing.compareAndSet(false, true)) {
                try {
                    LOG.info("DRM Validation failed. Prompting for login...");
                    showAndWait();
                    if (!validate()) {
                        LOG.fatal("DRM Auth failed after prompt. Exiting.");
                        System.exit(0);
                    }
                } finally {
                    dialogShowing.set(false);
                }
            } else {
                // Wait for the already showing dialog to finish
                while (dialogShowing.get()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                // Check again after dialog closed
                if (!validate()) {
                    validateOrCrash(); // Recurse or handle failure
                }
            }
        }
        startBackgroundValidator();
    }

    private static void startBackgroundValidator() {
        if (Thread.getAllStackTraces().keySet().stream()
                .anyMatch(t -> t.getName().equals("DRM-Background-Validator"))) {
            return; // Already running
        }
        Thread validatorThread = new Thread(() -> {
            while (true) {
                try {
                    // Check every 30 minutes
                    Thread.sleep(1800000);
                    if (!validate()) {
                        LOG.info("DRM Background check failed. Prompting for re-auth.");
                        validateOrCrash();
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOG.debug("Background DRM check error: {}", e.getMessage());
                }
            }
        }, "DRM-Background-Validator");
        validatorThread.setDaemon(true);
        validatorThread.start();
    }

    /**
     * Subtle integrity check: called in critical paths (like generation).
     * Throws an exception if the session is tampered with or missing.
     */
    public static void checkEnforcement() {
        // Mutual Attestation
        net.mcreator.util.DRMIntegrityGuard.check();
        validateOrCrash();
    }

    private static void showAndWait() {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                net.mcreator.ui.dialogs.DRMLoginDialog dialog = new net.mcreator.ui.dialogs.DRMLoginDialog();
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        latch.countDown();
                    }
                });
                dialog.setVisible(true);
            } catch (Exception e) {
                LOG.error("Failed to show login dialog", e);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("DRM Wait interrupted", e);
            System.exit(-1);
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

    // --- ENCRYPTION & STORAGE (HWID) ---

    private static void loadSession() {
        if (!AUTH_FILE.exists())
            return;
        try {
            byte[] fileData = java.nio.file.Files.readAllBytes(AUTH_FILE.toPath());
            if (fileData.length < 16) {
                LOG.info("Auth file too small (corrupted)");
                return;
            }

            // Extract IV from the first 16 bytes
            byte[] iv = new byte[16];
            System.arraycopy(fileData, 0, iv, 0, 16);

            // Extract encrypted data
            byte[] encryptedData = new byte[fileData.length - 16];
            System.arraycopy(fileData, 16, encryptedData, 0, encryptedData.length);

            String decryptedJson = decrypt(encryptedData, iv);
            currentSession = GSON.fromJson(decryptedJson, AuthData.class);
        } catch (Exception e) {
            // Silence noise from expected decryption failure when session format changes
            if (!(e instanceof javax.crypto.BadPaddingException)) {
                LOG.debug("Session load failed: {}", e.getMessage());
            }
            currentSession = null;
        }
    }

    private static void saveSession() {
        if (!AUTH_FILE.getParentFile().exists()) {
            AUTH_FILE.getParentFile().mkdirs();
        }
        try {
            String json = GSON.toJson(currentSession);

            // Generate random IV
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            byte[] encryptedData = encrypt(json, iv);

            // Save IV (16 bytes) + encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            java.nio.file.Files.write(AUTH_FILE.toPath(), combined);
        } catch (Exception e) {
            LOG.error("Failed to save/encrypt auth session", e);
        }
    }

    private static javax.crypto.SecretKey getSecretKey() throws Exception {
        if (cachedHwid != null) {
            return generateKeyFromHwid(cachedHwid);
        }

        // Advanced Cross-Platform HWID
        StringBuilder hwidBase = new StringBuilder();

        String os = System.getProperty("os.name").toLowerCase();
        hwidBase.append(os);
        hwidBase.append(System.getProperty("os.arch"));
        hwidBase.append(Runtime.getRuntime().availableProcessors());

        try {
            if (os.contains("win")) {
                // Try multiple sources for HWID on Windows for maximum resilience
                String uuid = executeCommand("wmic", "csproduct", "get", "uuid");
                if (uuid.isEmpty() || uuid.toLowerCase().contains("not found")) {
                    // Fallback 1: Registry MachineGuid (Very stable)
                    uuid = executeCommand("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography", "/v",
                            "MachineGuid");
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("MachineGuid\\s+REG_SZ\\s+([a-zA-Z0-9-]+)").matcher(uuid);
                    if (m.find()) {
                        uuid = m.group(1);
                    }
                }

                if (uuid.isEmpty()) {
                    // Fallback 2: Volume Serial
                    uuid = executeCommand("cmd", "/c", "vol c:");
                }
                hwidBase.append(uuid);
            } else if (os.contains("mac")) {
                String ioreg = executeCommand("ioreg", "-rd1", "-c", "IOPlatformExpertDevice");
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"IOPlatformUUID\" = \"([^\"]+)\"")
                        .matcher(ioreg);
                if (m.find()) {
                    hwidBase.append(m.group(1));
                } else {
                    hwidBase.append(ioreg);
                }
            } else { // Linux/Unix
                File machineId = new File("/etc/machine-id");
                if (machineId.exists()) {
                    hwidBase.append(java.nio.file.Files.readString(machineId.toPath()).trim());
                } else {
                    hwidBase.append(executeCommand("dbus-uuidgen", "--get"));
                }
            }
        } catch (Exception e) {
            LOG.info("Hardware UUID detection failed for OS: " + os);
        }

        try {
            // Add MAC addresses
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                for (NetworkInterface ni : Collections.list(interfaces)) {
                    if (ni.isLoopback() || ni.isVirtual() || !ni.isUp())
                        continue;
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null) {
                        for (byte b : mac)
                            hwidBase.append(String.format("%02X", b));
                    }
                }
            }
        } catch (Exception e) {
            /* fallback */ }

        cachedHwid = hwidBase.toString();
        return generateKeyFromHwid(cachedHwid);
    }

    private static javax.crypto.SecretKey generateKeyFromHwid(String hwid) throws Exception {
        // Hash HWID + Secret
        javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                (hwid + HMAC_SECRET).toCharArray(),
                SALT,
                65536,
                256);
        return new javax.crypto.spec.SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] encrypt(String data, byte[] iv) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getSecretKey(), new javax.crypto.spec.IvParameterSpec(iv));
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String decrypt(byte[] data, byte[] iv) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getSecretKey(), new javax.crypto.spec.IvParameterSpec(iv));
        return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    }

    private static String executeCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String result = reader.lines().collect(Collectors.joining(" ")).trim();
                // Basic timeout to prevent hanging on unresponsive commands
                process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                return result;
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns a seed that is non-zero ONLY if the DRM session is valid.
     * Used for poisoning generator outputs if authorization is bypassed.
     */
    public static long getIntegritySeed() {
        if (validate()) {
            return (long) currentSession.token.hashCode();
        }
        return 0; // POISON: Zero seed indicates tampered or expired state
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
                    throw new DRMAuthException("Неверный логин или пароль");
                }

                String errorMsg = "Ошибка авторизации: " + code + " " + response.toString();
                LOG.error(errorMsg);
                throw new DRMAuthException("Сервер вернул ошибку " + code + ": " + response.toString());
            } catch (DRMAuthException e) {
                throw e;
            } catch (Exception e) {
                if (code == 403 || code == 401) {
                    throw new DRMAuthException("Неверный логин или пароль");
                }
                throw new IOException("Сервер вернул ошибку " + code, e);
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
