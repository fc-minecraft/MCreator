package net.mcreator.ui.blockly;

import net.mcreator.ui.MCreator;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

public class MCRSchemeHandlerFactory implements CefSchemeHandlerFactory {

    // We can hold a reference to MCreator if needed, or fetch it dynamically if it's a singleton (it's not).
    // But SchemeHandlerFactory is global.
    // We can maintain a static reference to the "current" workspace or try to infer it.
    // Ideally, we should register the factory with the app.

    // Actually, CefApp is global. But we can register the factory once.
    // How do we get the workspace?
    // Maybe we don't need the workspace for icons? MCItem.getBlockIconBasedOnName requires it.
    // We can try to use a static helper in BlocklyPanel that exposes the current workspace.

    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        if (schemeName.equals("client") && request.getURL().startsWith("client://mcreator/")) {
            MCRResourceHandler handler = new MCRResourceHandler();
            // Try to find the associated workspace.
            // Since we can't easily map browser to workspace here without a map,
            // let's use a static accessor or accept that for now icons might be generic if we can't find it.
            // BUT: We can use `BlocklyPanel.getCurrentWorkspace()` if we implement it.
            // Or better: `browser` instance is passed. We can maintain a Map<CefBrowser, Workspace>.

            if (BlocklyPanel.browserWorkspaceMap.containsKey(browser)) {
                 handler.setWorkspace(BlocklyPanel.browserWorkspaceMap.get(browser));
            }

            return handler;
        }
        return null;
    }
}
