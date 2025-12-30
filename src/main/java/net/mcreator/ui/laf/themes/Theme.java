/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2021, Pylo, opensource contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.ui.laf.themes;

import net.mcreator.plugin.PluginLoader;
import net.mcreator.preferences.data.PreferencesData;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.util.ColorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A Theme can change images MCreator will use and redefine the colors and the style
 * of {@link net.mcreator.ui.blockly.BlocklyPanel} and {@link net.mcreator.ui.ide.RSyntaxTextAreaStyler} by creating a new {@link ColorScheme}</p>.
 */
@SuppressWarnings({ "unused", "FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection" })
public class Theme {

	private static final Logger LOG = LogManager.getLogger(Theme.class);

	public static Theme current() {
		return ThemeManager.CURRENT_THEME;
	}

	// Theme public model start
	private String name;
	@Nullable private String description;
	@Nullable private String version;
	@Nullable private String credits;

	@Nullable private ColorScheme colorScheme;

	private String flatLafTheme = "FlatDarkLaf";

	/**
	 * See <a href="https://www.formdev.com/flatlaf/properties-files/">FlatLaf properties file format</a>.
	 * <p/>
	 * One can add additional parameters from the theme JSON by defining them in the "flatLafOverrides" map.
	 */
	private Map<String, String> flatLafOverrides = new HashMap<>();

	private boolean disableMCreatorOverrides = false;
	// Theme public model end

	protected transient String id;
	private transient ImageIcon icon;
	private transient Font consoleFont;

	protected Theme init() {
		if (colorScheme != null)
			colorScheme.init();

		loadUIFonts();

		try {
			InputStream consoleFontStream = Theme.class.getResourceAsStream("/fonts/NotoSansMono-Regular.ttf");
			if (consoleFontStream != null) {
				consoleFont = Font.createFont(Font.TRUETYPE_FONT, consoleFontStream);
				consoleFont = consoleFont.deriveFont(12.0f);
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(consoleFont);
			} else {
				LOG.warn("Failed to load console font: NotoSansMono-Regular.ttf");
			}
		} catch (FontFormatException | IOException e) {
			LOG.error("Failed to init MCreator Theme Console Font! Error {}", e.getMessage());
		}

		return this;
	}

	public String getFlatLafTheme() {
		return flatLafTheme;
	}

	public void applyFlatLafOverrides(Map<String, String> overrides) {
		if (colorScheme != null) {
			overrides.put("@accentColor", ColorUtils.formatColor(Theme.current().getInterfaceAccentColor()));
			overrides.put("@background", ColorUtils.formatColor(Theme.current().getBackgroundColor()));
			overrides.put("@foreground", ColorUtils.formatColor(Theme.current().getForegroundColor()));
			overrides.put("@disabledBackground", ColorUtils.formatColor(Theme.current().getBackgroundColor()));
			overrides.put("@disabledForeground", ColorUtils.formatColor(Theme.current().getAltForegroundColor()));
			overrides.put("@selectionInactiveBackground", "@accentSelectionBackground");
			overrides.put("@selectionInactiveForeground", "@selectionForeground");

			// Color overrides for com.formdev.flatlaf.FlatIconColors used in SVG icons
			overrides.put("Objects.BlackText", "@foreground");
			overrides.put("Objects.Grey", "@buttonArrowColor");
			overrides.put("Objects.GreenAndroid", "@accentColor");
		} else {
            // Apply custom theme by default (FunCode Theme)
            overrides.put("@accentColor", "#CEFF32");
            overrides.put("@background", "#1E2A3C");
            overrides.put("@foreground", "#FFFFFF");
            overrides.put("Panel.background", "#1E2A3C");
            overrides.put("Label.foreground", "#FFFFFF");
            overrides.put("Button.background", "#53DDFF");
            overrides.put("Button.foreground", "#000000");

            overrides.put("Objects.BlackText", "@foreground");
			overrides.put("Objects.Grey", "@buttonArrowColor");
			overrides.put("Objects.GreenAndroid", "@accentColor");
        }

        // Increase font size
		overrides.put("defaultFont", "Noto Sans, 14");

		if (!disableMCreatorOverrides) {
			overrides.put("Button.arc", "0");
			overrides.put("Component.arc", "0");
			overrides.put("CheckBox.arc", "0");
			overrides.put("Spinner.arc", "0");
			overrides.put("ProgressBar.arc", "0");

			overrides.put("Component.focusWidth", "0");
			overrides.put("Component.innerFocusWidth", "0");

			overrides.put("Table.showHorizontalLines", "true");
			overrides.put("Table.showVerticalLines", "true");

			overrides.put("TabbedPane.contentOpaque", "false");
			overrides.put("Tree.rendererFillBackground", "false");

			overrides.put("TabbedPane.focusColor", "@background");

			overrides.put("List.focusCellHighlightBorder", "0,0,0,0");
			overrides.put("List.border", "0,0,0,0");
			overrides.put("ScrollPane.border", "0,0,0,0");
			overrides.put("Tree.border", "0,0,0,0");
			overrides.put("SplitPane.border", "0,0,0,0");
		}

		overrides.putAll(flatLafOverrides);
	}

	public void applyUIDefaultsOverrides(UIDefaults table) {
		table.put("FileChooser.homeFolderIcon", UIRES.get("laf.homeFolder"));
		table.put("FileChooser.newFolderIcon", UIRES.get("laf.newFolder"));
		table.put("FileChooser.upFolderIcon", UIRES.get("laf.upFolder"));
		table.put("FileChooser.computerIcon", UIRES.get("laf.computer"));
		table.put("FileChooser.hardDriveIcon", UIRES.get("laf.hardDrive"));
		table.put("FileChooser.floppyDriveIcon", UIRES.get("laf.floppy"));
		table.put("FileChooser.closedIcon", UIRES.get("laf.newFolder"));

		table.put("Tree.closedIcon", UIRES.get("laf.newFolder"));
		table.put("Tree.openIcon", UIRES.get("laf.upFolder"));
		table.put("Tree.leafIcon", UIRES.get("laf.file"));

		table.put("FileView.directoryIcon", UIRES.get("laf.directory"));
		table.put("FileView.fileIcon", UIRES.get("laf.file"));

		table.put("OptionPane.warningIcon", UIRES.get("laf.warning"));
		table.put("OptionPane.errorIcon", UIRES.get("laf.error"));
		table.put("OptionPane.questionIcon", UIRES.get("laf.question"));
		table.put("OptionPane.informationIcon", UIRES.get("laf.info"));
	}

	/**
	 * The ID is the theme's registry name. It is used to differentiate each theme in the code.
	 * This ID is also the main folder's name of the theme.
	 *
	 * @return <p>The theme's ID</p>
	 */
	public String getID() {
		return id;
	}

	/**
	 * @return <p>Its displayed name</p>
	 */
	private void loadUIFonts() {
		try {
			String[] fonts = {"NotoSans-Regular.ttf", "NotoSans-Bold.ttf", "NotoSans-Italic.ttf",
					"NotoSans-BoldItalic.ttf", "NotoSansMono-Regular.ttf", "NotoSansMono-Bold.ttf"};
			for (String fontName : fonts) {
				InputStream fontStream = Theme.class.getResourceAsStream("/fonts/" + fontName);
				if (fontStream != null) {
					Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
					GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
				} else {
					LOG.warn("Failed to load font: {}", fontName);
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to load UI fonts", e);
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * @return <p>A description displayed in the {@link net.mcreator.ui.dialogs.preferences.ThemesPanel} if provided.</p>
	 */
	public String getDescription() {
		// Description inside the JSON file
		if (description != null)
			return description;
			// Localized description
		else if (!L10N.t("theme." + id + ".description").equals("theme." + id + ".description"))
			return L10N.t("theme." + id + ".description");
			// No description
		else
			return "";
	}

	/**
	 * @return <p>A String with optional credits to give to someone.</p>
	 */
	@Nullable public String getCredits() {
		return credits;
	}

	/**
	 * @return <p>The theme's version if provided</p>
	 */
	@Nullable public String getVersion() {
		return version;
	}

	public Font getFont() {
		return UIManager.getFont("defaultFont");
	}

	public Font getConsoleFont() {
		return consoleFont;
	}

	/**
	 * This icon is only with {@link net.mcreator.ui.dialogs.preferences.ThemesPanel}.
	 *
	 * @return <p>An {@link ImageIcon} representing the plugin.</p>
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * <p>To be detected, the name of the image file needs to be "icon.png" located into the main folder.</p>
	 *
	 * @param icon <p>An {@link ImageIcon} to display in {@link net.mcreator.ui.dialogs.preferences.ThemesPanel}</p>
	 */
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	@Override public String toString() {
		return getID() + ": " + getName();
	}

	// Color scheme getters below (to keep code shorter)

	public Color getBackgroundColor() {
		if (colorScheme != null) {
			return colorScheme.getBackgroundColor();
		} else {
            return new Color(0x1E2A3C);
		}
	}

	/**
	 * @return Background of components (e.g. text fields, checkboxes and sound selectors)
	 */
	public Color getAltBackgroundColor() {
		if (colorScheme != null) {
			return colorScheme.getAltBackgroundColor();
		} else {
            return new Color(0x1E2A3C).brighter();
		}
	}

	/**
	 * @return Second background color used (e.g. workspace background)
	 */
	public Color getSecondAltBackgroundColor() {
		if (colorScheme != null) {
			return colorScheme.getSecondAltBackgroundColor();
		} else {
            return new Color(0x1E2A3C).darker();
		}
	}

	/**
	 * @return <p>Color used for most of texts </p>
	 */
	public Color getForegroundColor() {
		if (colorScheme != null) {
			return colorScheme.getForegroundColor();
		} else {
            return Color.WHITE;
		}
	}

	/**
	 * @return <p>Secondary text color </p>
	 */
	public Color getAltForegroundColor() {
		if (colorScheme != null) {
			return colorScheme.getAltForegroundColor();
		} else {
            return Color.LIGHT_GRAY;
		}
	}

	/**
	 * @return <p>Returns the interfaceAccentColor if defined by theme, otherwise the one defined by the user in {@link PreferencesData}</p>
	 */
	public Color getInterfaceAccentColor() {
		if (colorScheme != null) {
			return colorScheme.getInterfaceAccentColor();
		} else {
            return new Color(0xCEFF32);
		}
	}

}
