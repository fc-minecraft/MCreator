package net.mcreator.ui.init;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Integration test for DRM authentication.
 * Uses credentials from .env file to verify backend connectivity.
 */
public class DRMAuthIntegrationTest {

    private static final Logger LOG = LogManager.getLogger("DRMTest");

    @Test
    public void testLoginFlow() {
        String user = null;
        String pass = null;

        // Load from .env
        File envFile = new File(".env");
        if (envFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(envFile)) {
                props.load(fis);
                user = props.getProperty("DRM_TEST_USER");
                pass = props.getProperty("DRM_TEST_PASS");
            } catch (Exception e) {
                LOG.error("Failed to load .env file", e);
            }
        }

        // Fallback to Env vars (for CI/CD)
        if (user == null)
            user = System.getenv("DRM_TEST_USER");
        if (pass == null)
            pass = System.getenv("DRM_TEST_PASS");

        if (user == null || pass == null) {
            LOG.warn("DRM Test credentials missing, skipping integration test.");
            return;
        }

        LOG.info("Attempting DRM login for user: " + user);
        try {
            String result = DRMAuthManager.login(user, pass);
            Assertions.assertNotNull(result, "Login response should not be null");
            Assertions.assertTrue(result.contains("token"), "Response should contain access token");

            boolean isValid = DRMAuthManager.validate();
            Assertions.assertTrue(isValid, "Session should be valid after successful login");

            LOG.info("DRM Login Integration Test PASSED");
        } catch (Exception e) {
            Assertions.fail("DRM Login failed with exception: " + e.getMessage());
        } finally {
            // Cleanup: remove the test session to avoid polluting developers environment
            DRMAuthManager.logout();
        }
    }
}
