package net.mcreator.ui.blockly;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.IProgressHandler;
import me.friwi.jcefmaven.EnumProgress;
import net.mcreator.io.FileIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefMessageRouter;

import java.io.File;

public class JCEFHelper {

    private static final Logger LOG = LogManager.getLogger("JCEF");
    private static CefApp cefApp;
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) return;

        try {
            CefAppBuilder builder = new CefAppBuilder();
            builder.setInstallDir(new File("jcef-bundle"));
            builder.setProgressHandler(new IProgressHandler() {
                @Override
                public void handleProgress(EnumProgress state, float percent) {
                    LOG.info("JCEF Init: " + state + " " + percent + "%");
                }
            });

            // Configure command line arguments for CEF
            builder.getCefSettings().windowless_rendering_enabled = false;

            builder.setAppHandler(new MavenCefAppHandlerAdapter() {
                @Override
                public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                   if (state == CefApp.CefAppState.TERMINATED) {
                       // Handle termination if needed
                   }
                }
            });

            cefApp = builder.build();
            initialized = true;

        } catch (Exception e) {
            LOG.error("Failed to initialize JCEF", e);
        }
    }

    public static CefApp getCefApp() {
        if (!initialized) {
            initialize();
        }
        return cefApp;
    }

    public static CefClient createClient() {
        CefApp app = getCefApp();
        if (app != null) {
            return app.createClient();
        }
        return null;
    }
}
