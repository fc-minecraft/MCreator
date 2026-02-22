package net.mcreator.ui.init;

import java.io.IOException;

/**
 * Custom exception for DRM-related authentication and authorization failures.
 * These exceptions are intended to be filtered out from automated crash
 * reporting systems like Sentry.
 */
public class DRMAuthException extends IOException {

    public DRMAuthException(String message) {
        super(message);
    }

    public DRMAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
