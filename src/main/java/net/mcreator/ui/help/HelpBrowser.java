package net.mcreator.ui.help;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.mcreator.io.FileIO;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.util.DesktopUtils;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class HelpBrowser extends JFrame {

    private JEditorPane contentPane;
    private JTree navigationTree;

    private static HelpBrowser INSTANCE;
    private static java.util.Map<String, String> SLUG_CACHE;

    public static void open() {
        openPage(null);
    }

    public static void openPage(String page) {
        if (INSTANCE != null && !INSTANCE.isVisible()) {
            INSTANCE.dispose();
            INSTANCE = null;
        }
        if (INSTANCE == null) {
            INSTANCE = new HelpBrowser();
        }
        INSTANCE.setVisible(true);
        INSTANCE.toFront();
        if (page != null) {
            INSTANCE.loadPage(page);
        }
    }

    private HelpBrowser() {
        setTitle(L10N.t("help_browser.title"));
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setIconImage(UIRES.get("help").getImage());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Content Pane
        contentPane = new JEditorPane();
        contentPane.setContentType("text/html");
        contentPane.setEditable(false);
        contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        ComponentUtils.deriveFont(contentPane, 14); // Readable font size

        // Custom CSS for better look
        try {
            javax.swing.text.html.HTMLEditorKit kit = new javax.swing.text.html.HTMLEditorKit();
            kit.getStyleSheet().addRule("body { font-family: Segoe UI, Open Sans, sans-serif; font-size: 14px; margin: 10px; color: #333; }");
            kit.getStyleSheet().addRule("h1 { font-size: 24px; color: #000; margin-bottom: 10px; }");
            kit.getStyleSheet().addRule("h2 { font-size: 20px; color: #444; margin-top: 15px; margin-bottom: 5px; }");
            kit.getStyleSheet().addRule("h3 { font-size: 16px; font-weight: bold; margin-top: 10px; }");
            kit.getStyleSheet().addRule("code { background-color: #f0f0f0; font-family: Monospaced; }");
            kit.getStyleSheet().addRule("a { color: #0066cc; text-decoration: none; }");
            contentPane.setEditorKit(kit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc != null && !desc.contains("://") && !desc.startsWith("mailto:")) {
                    loadPage(desc);
                } else if (e.getURL() != null) {
                    DesktopUtils.browseSafe(e.getURL().toString());
                }
            }
        });

        JScrollPane contentScroll = new JScrollPane(contentPane);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());

        // Navigation Tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(L10N.t("help_browser.home"));
        createTreeStructure(root);
        navigationTree = new JTree(root);
        navigationTree.addTreeSelectionListener(this::onNodeSelected);
        navigationTree.setRowHeight(24);
        ComponentUtils.deriveFont(navigationTree, 13);

        JScrollPane treeScroll = new JScrollPane(navigationTree);
        treeScroll.setPreferredSize(new Dimension(250, 0));
        treeScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, contentScroll);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.0); // Keep tree size constant on resize

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);

        // Load initial page
        loadPage("index.md");
    }

    private void createTreeStructure(DefaultMutableTreeNode root) {
        try {
            InputStream is = getClass().getResourceAsStream("/help/toc.json");
            if (is != null) {
                Gson gson = new Gson();
                List<TocEntry> toc = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<List<TocEntry>>(){}.getType());
                for (TocEntry entry : toc) {
                    addTocEntry(root, entry);
                }
            } else {
                root.add(new DefaultMutableTreeNode("TOC not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            root.add(new DefaultMutableTreeNode("Error loading TOC"));
        }
    }

    private void addTocEntry(DefaultMutableTreeNode parent, TocEntry entry) {
        String title = entry.title;
        String translated = L10N.t(title);
        if (translated != null) {
            title = translated;
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new HelpNode(title, entry.file));
        parent.add(node);
        if (entry.children != null) {
            for (TocEntry child : entry.children) {
                addTocEntry(node, child);
            }
        }
    }

    private void onNodeSelected(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) navigationTree.getLastSelectedPathComponent();
        if (node == null) return;

        Object userObject = node.getUserObject();
        if (userObject instanceof HelpNode helpNode) {
            if (helpNode.filename != null) {
                loadPage(helpNode.filename);
            }
        } else if (node.isRoot()) {
            loadPage("index.md");
        }
    }

    private void loadPage(String slug) {
        String filename = getFilenameForSlug(slug);
        try {
            // Try localized first
            String path = "/help/" + net.mcreator.ui.init.L10N.getLocaleString() + "/wiki/" + filename;
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                // Fallback to default
                path = "/help/default/wiki/" + filename;
                is = getClass().getResourceAsStream(path);
            }

            if (is != null) {
                String mdContent;
                try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                    mdContent = scanner.useDelimiter("\\A").next();
                }

                String html = renderMarkdown(mdContent);
                contentPane.setText(html);
                contentPane.setCaretPosition(0);
            } else {
                contentPane.setText("<html><body><h1>Page not found</h1><p>Could not load " + filename + "</p></body></html>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentPane.setText("<html><body><h1>Error</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    private String renderMarkdown(String md) {
        List<Extension> extensions = Arrays.asList(TablesExtension.create(), AutolinkExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        return "<html><body>" + renderer.render(parser.parse(md)) + "</body></html>";
    }

    private record HelpNode(String title, String filename) {
        @Override
        public String toString() {
            return title;
        }
    }

    private static class TocEntry {
        String title;
        String file;
        List<TocEntry> children;
    }

    private String getFilenameForSlug(String slug) {
        if (SLUG_CACHE == null) {
            try {
                InputStream is = getClass().getResourceAsStream("/help/slug_map.json");
                if (is != null) {
                    Gson gson = new Gson();
                    SLUG_CACHE = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<java.util.Map<String, String>>(){}.getType());
                } else {
                    SLUG_CACHE = new java.util.HashMap<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SLUG_CACHE = new java.util.HashMap<>();
            }
        }

        if (SLUG_CACHE.containsKey(slug)) {
            return SLUG_CACHE.get(slug);
        }
        // Fallback: assume slug is filename if it ends in .md, else append .md
        return slug.endsWith(".md") ? slug : slug + ".md";
    }
}
