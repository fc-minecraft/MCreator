package net.mcreator.drm;

import net.mcreator.ui.init.DRMAuthManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

public class DRMAuthTest {

    @Test
    public void testDaysRemainingCalculation() {
        // Ensure clean state
        DRMAuthManager.logout();
        Assertions.assertEquals(0, DRMAuthManager.getDaysRemaining());
    }

    @Test
    public void testLogoutClearsSession() {
        // Create a dummy auth file to simulate a session
        File authFile = new File(System.getProperty("user.home"), ".mcreator/drm_auth.bin");
        try {
            // We can't easily write a valid signed file without exposing logic,
            // but we can verify logout deletes *any* file there.
            if (!authFile.getParentFile().exists())
                authFile.getParentFile().mkdirs();
            Files.write(authFile.toPath(), "{\"data\":{}, \"signature\":\"invalid\"}".getBytes());

            Assertions.assertTrue(authFile.exists(), "Setup: Auth file should exist");

            DRMAuthManager.logout();

            Assertions.assertFalse(authFile.exists(), "Auth file should be deleted after logout");
        } catch (Exception e) {
            Assertions.fail("Failed to setup test", e);
        }
    }
}
