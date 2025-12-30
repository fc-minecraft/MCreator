/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
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

package net.mcreator.ui.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {

	private static final Logger LOG = LogManager.getLogger("Font Loader");

	public static void loadFonts() {
		try {
			String[] fonts = {
					"NotoSans-Regular.ttf", "NotoSans-Bold.ttf", "NotoSans-Italic.ttf", "NotoSans-BoldItalic.ttf",
					"NotoSansMono-Regular.ttf", "NotoSansMono-Bold.ttf"
			};
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

			for (String fontName : fonts) {
				try (InputStream fontStream = FontLoader.class.getResourceAsStream("/fonts/" + fontName)) {
					if (fontStream != null) {
						Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
						ge.registerFont(font);
					} else {
						LOG.warn("Failed to find font resource: {}", fontName);
					}
				} catch (FontFormatException | IOException e) {
					LOG.error("Failed to load font: {}", fontName, e);
				}
			}
		} catch (Exception e) {
			LOG.error("Critical failure in FontLoader", e);
		}
	}

}
