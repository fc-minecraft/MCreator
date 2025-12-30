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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.preferences.PreferencesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class UIRES {

	private static final Logger LOG = LogManager.getLogger("UIRES");

	// Use SoftReference to allow GC to reclaim memory if needed
	private static final Map<String, SoftReference<ImageIcon>> THEME_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, SoftReference<ImageIcon>> FALLBACK_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, SoftReference<ImageIcon>> BUILTIN_CACHE = new ConcurrentHashMap<>();

	// Cache to store missing images to avoid repeated failed lookups
	private static final Map<String, Boolean> MISSING_CACHE = new ConcurrentHashMap<>();

	private static final Pattern rasterPattern = Pattern.compile(".*\\.png$");
	private static final Pattern vectorPattern = Pattern.compile(".*\\.svg$");

	public static void preloadImages() {
		// Lazy loading implemented, no preloading needed.
		// This method is kept for compatibility but does nothing to save startup time.
		LOG.info("Lazy image loading enabled. Skipping preload.");
	}

	private static ImageIcon loadFromTheme(String theme, String identifier, String type) {
		String themePath = "themes." + theme + ".images." + identifier + "." + type;

		String resourcePath = "themes/" + theme + "/images/" + identifier.replace('.', '/') + "." + type;

		URL url = PluginLoader.INSTANCE.getResource(resourcePath);
		if (url != null) {
			if ("svg".equals(type)) {
				return new FlatSVGIcon(url);
			} else {
				return new ImageIcon(url);
			}
		}
		return null;
	}

	/**
	 * Gets an image from the current theme or the default theme if the image is not found.
	 * </p>
	 * Image loading priority (top down, first match found):
	 * <ol>
	 *     <li>Current theme SVG</li>
	 *     <li>Current theme PNG</li>
	 *     <li>Default theme SVG</li>
	 *     <li>Default theme PNG</li>
	 *     <li>Throws NullPointerException</li>
	 * </ol>
	 *
	 * @param identifier the identifier of the image
	 * @return the image icon
	 */
	public static ImageIcon get(String identifier) {
		if (MISSING_CACHE.containsKey(identifier)) {
			throw new NullPointerException("Image not found (cached missing): " + identifier);
		}

		String currentTheme = PreferencesManager.PREFERENCES.hidden.uiTheme.get();
		String defaultTheme = "default_dark";

		// Check caches first
		ImageIcon cached = checkCache(identifier, currentTheme);
		if (cached != null) return cached;

		// Load from current theme
		ImageIcon currentSvg = loadAndCache(currentTheme, identifier, "svg", THEME_CACHE);
		if (currentSvg != null) return currentSvg;

		ImageIcon currentPng = loadAndCache(currentTheme, identifier, "png", THEME_CACHE);
		if (currentPng != null) return currentPng;

		// Load from fallback theme
		if (!currentTheme.equals(defaultTheme)) {
			ImageIcon fallbackSvg = loadAndCache(defaultTheme, identifier, "svg", FALLBACK_CACHE);
			if (fallbackSvg != null) return fallbackSvg;

			ImageIcon fallbackPng = loadAndCache(defaultTheme, identifier, "png", FALLBACK_CACHE);
			if (fallbackPng != null) return fallbackPng;
		}

		MISSING_CACHE.put(identifier, true);
		throw new NullPointerException("Image not found: " + identifier);
	}

	private static ImageIcon checkCache(String identifier, String currentTheme) {
		String keySvg = identifier + ".svg";
		String keyPng = identifier + ".png";

		ImageIcon icon;
		if ((icon = getFromSoftCache(THEME_CACHE, keySvg)) != null) return icon;
		if ((icon = getFromSoftCache(THEME_CACHE, keyPng)) != null) return icon;

		if (!currentTheme.equals("default_dark")) {
			if ((icon = getFromSoftCache(FALLBACK_CACHE, keySvg)) != null) return icon;
			if ((icon = getFromSoftCache(FALLBACK_CACHE, keyPng)) != null) return icon;
		}
		return null;
	}

	private static ImageIcon getFromSoftCache(Map<String, SoftReference<ImageIcon>> cache, String key) {
		SoftReference<ImageIcon> ref = cache.get(key);
		if (ref != null) {
			return ref.get();
		}
		return null;
	}

	private static ImageIcon loadAndCache(String theme, String identifier, String type, Map<String, SoftReference<ImageIcon>> cache) {
		String key = identifier + "." + type;

		// Double check cache
		ImageIcon cached = getFromSoftCache(cache, key);
		if (cached != null) return cached;

		ImageIcon icon = loadFromTheme(theme, identifier, type);
		if (icon != null) {
			cache.put(key, new SoftReference<>(icon));
			return icon;
		}
		return null;
	}

	/**
	 * Returns a built-in image from the MCreator UI resources. Only works for raster images.
	 * Use {@link SVG} for vector images.
	 *
	 * @param identifier the identifier of the image
	 * @return the image icon
	 */
	public static ImageIcon getBuiltIn(String identifier) {
		String key = "@" + identifier;
		ImageIcon icon = getFromSoftCache(BUILTIN_CACHE, key);
		if (icon != null) return icon;

		icon = new ImageIcon(Objects.requireNonNull(
				ClassLoader.getSystemClassLoader().getResource("net/mcreator/ui/res/" + identifier + ".png")));
		BUILTIN_CACHE.put(key, new SoftReference<>(icon));
		return icon;
	}

	public static class SVG {

		private static final Map<String, SoftReference<ImageIcon>> CACHE = new ConcurrentHashMap<>();

		public static ImageIcon getBuiltIn(String identifier, int width, int height) {
			return getBuiltIn(identifier, width, height, null);
		}

		public static ImageIcon getBuiltIn(String identifier, int width, int height, @Nullable Color paint) {
			String key = computeKey("@" + identifier, width, height, paint);
			SoftReference<ImageIcon> ref = CACHE.get(key);
			if (ref != null) {
				ImageIcon icon = ref.get();
				if (icon != null) return icon;
			}

			URL url = ClassLoader.getSystemClassLoader()
					.getResource("net/mcreator/ui/res/" + identifier.replace('.', '/') + ".svg");
			ImageIcon icon = new FlatSVGIcon(url).derive(width, height);

			if (icon != null)
				CACHE.put(key, new SoftReference<>(icon));

			return icon;
		}

		private static String computeKey(String identifier, int width, int height, @Nullable Color color) {
			//@formatter:off
			return identifier + ".svg" +
					(width != 0 ? ("." + width) : "") +
					(height != 0 ? ("." + height) : "") +
					(color == null ? "" : ("." + color.getRGB()));
			//@formatter:on
		}

	}

}
