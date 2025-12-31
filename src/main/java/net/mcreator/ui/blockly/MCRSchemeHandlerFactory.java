package net.mcreator.ui.blockly;

import net.mcreator.ui.MCreator;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

public class MCRSchemeHandlerFactory implements CefSchemeHandlerFactory {

    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        if (schemeName.equals("http") && request.getURL().startsWith("http://mcreator.local/")) {
            MCRResourceHandler handler = new MCRResourceHandler();
            if (BlocklyPanel.browserWorkspaceMap.containsKey(browser)) {
                 handler.setWorkspace(BlocklyPanel.browserWorkspaceMap.get(browser));
            }
            return handler;
        }
        return null;
    }
}
