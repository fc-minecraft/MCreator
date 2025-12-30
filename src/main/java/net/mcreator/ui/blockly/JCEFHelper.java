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
import java.util.function.Consumer;

public class JCEFHelper {

    private static final Logger LOG = LogManager.getLogger("JCEF");
    private static CefApp cefApp;
    private static boolean initialized = false;
    private static boolean initializing = false;
    private static final Object lock = new Object();

    public static void initialize(Consumer<String> statusUpdater) {
        synchronized (lock) {
            if (initialized) return;
            if (initializing) {
                // Wait for other thread to finish
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
            initializing = true;
        }

        try {
            CefAppBuilder builder = new CefAppBuilder();
            builder.setInstallDir(new File("jcef-bundle"));
            builder.setProgressHandler(new IProgressHandler() {
                @Override
                public void handleProgress(EnumProgress state, float percent) {
                    String msg = "JCEF Init: " + state + " " + ((int)percent) + "%";
                    LOG.info(msg);
                    if (statusUpdater != null) {
                         statusUpdater.accept(msg);
                    }
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

            // Register custom scheme immediately after build
            cefApp.registerSchemeHandlerFactory("client", "mcreator", new MCRSchemeHandlerFactory());

            synchronized (lock) {
                initialized = true;
                initializing = false;
                lock.notifyAll();
            }

        } catch (Exception e) {
            LOG.error("Failed to initialize JCEF", e);
            synchronized (lock) {
                initializing = false; // Allow retry?
                lock.notifyAll();
            }
        }
    }

    public static CefApp getCefApp() {
        synchronized (lock) {
            while (initializing) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
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
