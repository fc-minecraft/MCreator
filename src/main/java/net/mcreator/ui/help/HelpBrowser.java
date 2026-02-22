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

package net.mcreator.ui.help;

import net.mcreator.util.DesktopUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;

public class HelpBrowser {

	private static final Logger LOG = LogManager.getLogger(HelpBrowser.class);

	public static void open(String url) {
		String slug = extractSlug(url);
		File localFile = new File("plugins/mcreator-localization/help/ru_RU/wiki/" + slug + ".html");

		if (!localFile.exists()) {
			// Try fallback to index if exact slug not found (e.g. root wiki url)
			if (slug.isEmpty() || slug.equals("index")) {
				localFile = new File("plugins/mcreator-localization/help/ru_RU/wiki/index.html");
			}
		}

		if (localFile.exists()) {
			LOG.info("Opening local wiki page: " + localFile.getAbsolutePath());
			DesktopUtils.openSafe(localFile);
		} else {
			LOG.warn("Local help file not found for slug: " + slug + ". Path: " + localFile.getAbsolutePath());
			// Fallback: Open the index page so the user can search, or try online if preferred?
			// Since we want offline, let's open index with a potential warning logic if we had a UI,
			// but here just opening index is safer than failing silently.
			File indexFile = new File("plugins/mcreator-localization/help/ru_RU/wiki/index.html");
			if (indexFile.exists()) {
				DesktopUtils.openSafe(indexFile);
			} else {
				// Last resort: Open the online URL if local index is also missing
				DesktopUtils.browseSafe(url);
			}
		}
	}

	private static String extractSlug(String url) {
		if (url.contains("/wiki/")) {
			String[] parts = url.split("/wiki/");
			if (parts.length > 1) {
				String slug = parts[1];
				if (slug.contains("#")) slug = slug.substring(0, slug.indexOf("#"));
				if (slug.contains("?")) slug = slug.substring(0, slug.indexOf("?"));
				if (slug.endsWith("/")) slug = slug.substring(0, slug.length() - 1);
				return slug;
			}
		}
		if (url.endsWith("mcreator.net/wiki") || url.endsWith("mcreator.net/wiki/")) {
			return "";
		}
		return "";
	}
}
