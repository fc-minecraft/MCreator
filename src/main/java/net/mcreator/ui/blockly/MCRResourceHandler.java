package net.mcreator.ui.blockly;

import net.mcreator.minecraft.MCItem;
import net.mcreator.minecraft.MinecraftImageGenerator;
import net.mcreator.util.image.ImageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class MCRResourceHandler implements CefResourceHandler {

    private static final Logger LOG = LogManager.getLogger("BlocklyRes");
    private byte[] data;
    private String mimeType;
    private int offset = 0;
    private net.mcreator.workspace.Workspace workspace;

    public void setWorkspace(net.mcreator.workspace.Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean processRequest(CefRequest request, CefCallback callback) {
        try {
            String url = request.getURL();
            // Возвращаемся к mcreator.ui - это безопасный фейковый домен
            String path = url.replace("http://mcreator.ui/", "");
            
            if (path.contains("?")) path = path.substring(0, path.indexOf("?"));
            if (path.contains("#")) path = path.substring(0, path.indexOf("#"));
            
            try {
                path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                LOG.warn("Failed to decode path: " + path);
            }

            if (path.startsWith("icon/")) {
                String itemName = path.substring(5);
                if (itemName.endsWith(".png")) itemName = itemName.substring(0, itemName.length() - 4);
                try {
                    data = generateIcon(itemName);
                    mimeType = "image/png";
                    callback.Continue();
                    return true;
                } catch (Exception e) {
                    LOG.error("Failed to generate icon: " + itemName, e);
                }
            }

            if (path.startsWith("/")) path = path.substring(1);
            String resourcePath = "/" + path;

            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    data = is.readAllBytes(); // Java 9+ метод
                    mimeType = determineMimeType(path);
                    callback.Continue();
                    return true;
                } else {
                    LOG.error("Resource NOT FOUND: " + resourcePath + " (URL: " + url + ")");
                }
            } catch (IOException e) {
                LOG.error("Error reading resource: " + resourcePath, e);
            }

            String errorMsg = "<html><body style='background:#222;color:#f55'><h1>404 Not Found</h1><p>" + resourcePath + "</p></body></html>";
            data = errorMsg.getBytes(StandardCharsets.UTF_8);
            mimeType = "text/html";
            
        } catch (Throwable t) {
            LOG.error("CRITICAL ERROR in processRequest", t);
            String errorMsg = "<html><body><h1>500 Internal Error</h1><pre>" + t.toString() + "</pre></body></html>";
            data = errorMsg.getBytes(StandardCharsets.UTF_8);
            mimeType = "text/html";
        }

        callback.Continue();
        return true;
    }

    private byte[] generateIcon(String name) throws IOException {
        ImageIcon base = new ImageIcon(ImageUtils.resize(MinecraftImageGenerator.generateItemSlot(), 36, 36));
        ImageIcon image;
        if (workspace != null && name != null && !name.isEmpty() && !name.equals("null"))
            image = ImageUtils.drawOver(base, MCItem.getBlockIconBasedOnName(workspace, name), 2, 2, 32, 32);
        else
            image = base;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(ImageUtils.toBufferedImage(image.getImage()), "PNG", os);
        return os.toByteArray();
    }

    private String determineMimeType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }

    @Override
    public void getResponseHeaders(CefResponse response, IntRef response_length, StringRef redirectUrl) {
        if (data == null) {
            response.setStatus(404);
            response_length.set(0);
            return;
        }
        response.setMimeType(mimeType);
        response.setStatus(200);
        response_length.set(data.length);
    }

    @Override
    public boolean readResponse(byte[] data_out, int bytes_to_read, IntRef bytes_read, CefCallback callback) {
        if (data == null || offset >= data.length) {
            bytes_read.set(0);
            return false;
        }
        int length = Math.min(bytes_to_read, data.length - offset);
        System.arraycopy(data, offset, data_out, 0, length);
        offset += length;
        bytes_read.set(length);
        return true;
    }

    @Override
    public void cancel() { }
}