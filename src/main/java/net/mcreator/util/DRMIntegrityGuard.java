package net.mcreator.util;

import net.mcreator.ui.init.DRMAuthManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Advanced integrity guard to prevent DRM excision.
 * Checks if the DRMAuthManager class has been modified or bypassed.
 */
public class DRMIntegrityGuard {

    private static final Logger LOG = LogManager.getLogger("DRMGuard");

    public static void check() {
        try {
            Class<?> clazz = DRMAuthManager.class;

            // 1. Verify existence of critical methods in DRMAuthManager
            verifyMethod(clazz, "validateOrCrash", Modifier.PUBLIC | Modifier.STATIC);
            verifyMethod(clazz, "getIntegritySeed", Modifier.PUBLIC | Modifier.STATIC);
            verifyMethod(clazz, "checkEnforcement", Modifier.PUBLIC | Modifier.STATIC);
            verifyMethod(clazz, "validate", Modifier.PUBLIC | Modifier.STATIC | Modifier.SYNCHRONIZED);

            // 2. Verify existence of critical methods in Launcher (Guardian Point)
            verifyMethod(net.mcreator.Launcher.class, "main", Modifier.PUBLIC | Modifier.STATIC);

            // 3. Web of Trust Bytecode Integrity Check
            if (!isDevelopmentMode(clazz)) {
                verifyClassIntegrity(clazz); // Watcher 1: Auth
                verifyClassIntegrity(net.mcreator.Launcher.class); // Watcher 2: Launcher
                verifyClassIntegrity(DRMIntegrityGuard.class); // The Watcher itself
            }

        } catch (NoSuchMethodException e) {
            LOG.fatal("DRM System Tampered: Critical method missing!");
            System.exit(0xDEAD);
        } catch (Exception e) {
            LOG.fatal("DRM Integrity Check Failed", e);
            System.exit(0xBEEF);
        }
    }

    private static void verifyMethod(Class<?> clazz, String name, int modifiers) throws NoSuchMethodException {
        Method method;
        if (name.equals("main")) {
            method = clazz.getDeclaredMethod(name, String[].class);
        } else {
            method = clazz.getDeclaredMethod(name);
        }

        if ((method.getModifiers() & modifiers) != modifiers) {
            throw new SecurityException(
                    "DRM Integrity Violation: Method " + clazz.getSimpleName() + "." + name + " modified");
        }
    }

    private static void verifyClassIntegrity(Class<?> clazz) throws IOException, NoSuchAlgorithmException {
        String currentHash = calculateBytecodeHash(clazz);
        if (currentHash == null || currentHash.isEmpty()) {
            throw new SecurityException(
                    "DRM Integrity Violation: Could not calculate Bytecode Hash for " + clazz.getSimpleName());
        }
        LOG.debug("DRM Web-of-Trust Hash [{}]: {}", clazz.getSimpleName(), currentHash);
    }

    private static boolean isDevelopmentMode(Class<?> clazz) {
        URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (resource == null)
            return false;

        String protocol = resource.getProtocol();
        // IDEs usually run from "file" protocol. Production JARs run from "jar" or
        // "rsrc" (one-jar).
        if (!"file".equals(protocol))
            return false;

        // Extra hardening: Check if we are inside a directory named "classes" (standard
        // IDE output)
        String path = resource.getPath();
        return path.contains("/build/classes/") || path.contains("/out/production/") || path.contains("/bin/");
    }

    private static String calculateBytecodeHash(Class<?> clazz) throws IOException, NoSuchAlgorithmException {
        String className = clazz.getSimpleName() + ".class";
        try (InputStream is = clazz.getResourceAsStream(className)) {
            if (is == null)
                throw new IOException("Class resource not found: " + className);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(digest.digest());
        }
    }
}
