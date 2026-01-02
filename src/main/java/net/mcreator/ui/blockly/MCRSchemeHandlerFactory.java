package net.mcreator.ui.blockly;

import net.mcreator.ui.MCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;

public class MCRSchemeHandlerFactory implements CefSchemeHandlerFactory {

    private static final Logger LOG = LogManager.getLogger("BlocklyFactory");

    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        String url = request.getURL();
        
        // Перехватываем localhost
        if (schemeName.equals("http") && url.startsWith("http://localhost/")) {
            LOG.debug("INTERCEPTED: " + url); 
            MCRResourceHandler handler = new MCRResourceHandler();
            if (BlocklyPanel.browserWorkspaceMap.containsKey(browser)) {
                 handler.setWorkspace(BlocklyPanel.browserWorkspaceMap.get(browser));
            }
            return handler;
        }
        return null;
    }
}