package net.mcreator.ui.init;

import net.mcreator.util.DRMIntegrityGuard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class DRMHardeningDeepTest {

    @Test
    public void testHwidConsistency() throws Exception {
        // HWID must be identical across multiple calls in the same environment
        Method getSecretKeyMethod = DRMAuthManager.class.getDeclaredMethod("getSecretKey");
        getSecretKeyMethod.setAccessible(true);

        SecretKey key1 = (SecretKey) getSecretKeyMethod.invoke(null);
        SecretKey key2 = (SecretKey) getSecretKeyMethod.invoke(null);

        Assertions.assertArrayEquals(key1.getEncoded(), key2.getEncoded(), "HWID-derived key must be consistent");
    }

    @Test
    public void testEncryptionIntegrity() throws Exception {
        // Verify that data can be round-tripped through the encryption system
        Method saveMethod = DRMAuthManager.class.getDeclaredMethod("saveSession");
        Method loadMethod = DRMAuthManager.class.getDeclaredMethod("loadSession");
        saveMethod.setAccessible(true);
        loadMethod.setAccessible(true);

        // Mock a session data object
        // Note: Using reflection to create instance because AuthData might be
        // private/inner
        Class<?> authDataClass = Class.forName("net.mcreator.ui.init.DRMAuthManager$AuthData");
        java.lang.reflect.Constructor<?> constructor = authDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object data = constructor.newInstance();

        Field tokenField = authDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        String testToken = "test_token_" + System.currentTimeMillis();
        tokenField.set(data, testToken);

        // Use reflection to set session in DRMAuthManager
        Field sessionField = DRMAuthManager.class.getDeclaredField("currentSession");
        sessionField.setAccessible(true);
        sessionField.set(null, data);

        saveMethod.invoke(null);
        sessionField.set(null, null); // Clear in-memory to force reload

        loadMethod.invoke(null);

        Object loaded = sessionField.get(null);
        Assertions.assertNotNull(loaded, "Session must be successfully decrypted and loaded from disk");
        Assertions.assertEquals(testToken, tokenField.get(loaded), "Decrypted token must match original");
    }

    @Test
    public void testIntegrityGuardReflexivity() {
        // Ensure the guard doesn't fail on a clean environment
        Assertions.assertDoesNotThrow(DRMIntegrityGuard::check, "Integrity guard should pass on unmodified codebase");
    }

    @Test
    public void testIntegritySeedLogic() throws Exception {
        // Mock a valid session
        Class<?> authDataClass = Class.forName("net.mcreator.ui.init.DRMAuthManager$AuthData");
        java.lang.reflect.Constructor<?> constructor = authDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object data = constructor.newInstance();

        Field tokenField = authDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(data, "valid_token");

        Field expireField = authDataClass.getDeclaredField("authExpire");
        expireField.setAccessible(true);
        expireField.set(data, java.time.Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS).toString());

        Field sessionField = DRMAuthManager.class.getDeclaredField("currentSession");
        sessionField.setAccessible(true);
        sessionField.set(null, data);

        // Integrity seed must be non-zero if a session is present and valid
        long seed = DRMAuthManager.getIntegritySeed();
        Assertions.assertNotEquals(0, seed, "Integrity seed should be non-zero for valid session");
    }

    @Test
    public void testExpirationLogic() throws Exception {
        Class<?> authDataClass = Class.forName("net.mcreator.ui.init.DRMAuthManager$AuthData");
        java.lang.reflect.Constructor<?> constructor = authDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object data = constructor.newInstance();

        Field expireField = authDataClass.getDeclaredField("authExpire");
        expireField.setAccessible(true);
        // Set expiration to 1 hour ago
        expireField.set(data, java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS).toString());

        Field tokenField = authDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(data, "expired_token");

        Field sessionField = DRMAuthManager.class.getDeclaredField("currentSession");
        sessionField.setAccessible(true);
        sessionField.set(null, data);

        Assertions.assertFalse(DRMAuthManager.validate(), "Expired session should be invalid");
        Assertions.assertEquals(0, DRMAuthManager.getIntegritySeed(), "Expired session should return 0 integrity seed");
    }
}
