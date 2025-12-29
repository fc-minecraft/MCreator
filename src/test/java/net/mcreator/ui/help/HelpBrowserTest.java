package net.mcreator.ui.help;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.mcreator.io.FileIO;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HelpBrowserTest {

    @Test
    void testMarkdownRendering() {
        String md = "# Hello\n\n* Item 1\n* Item 2";
        List<Extension> extensions = Arrays.asList(TablesExtension.create(), AutolinkExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        String html = renderer.render(parser.parse(md));

        assertTrue(html.contains("<h1>Hello</h1>"));
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>Item 1</li>"));
    }

    @Test
    void testTocJsonParsing() {
        InputStream is = getClass().getResourceAsStream("/help/toc.json");
        assertNotNull(is, "toc.json should exist in resources");

        Gson gson = new Gson();
        List<TocEntry> toc = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<List<TocEntry>>(){}.getType());
        assertNotNull(toc);
        assertFalse(toc.isEmpty());
        assertEquals("help_browser.toc.home", toc.get(0).title);
    }

    @Test
    void testSlugMapParsing() {
        InputStream is = getClass().getResourceAsStream("/help/slug_map.json");
        assertNotNull(is, "slug_map.json should exist in resources");

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>(){}.getType());
        assertNotNull(map);
        assertEquals("index.md", map.get("index"));
        assertEquals("block.md", map.get("block"));
    }

    private static class TocEntry {
        String title;
        String file;
        List<TocEntry> children;
    }
}
