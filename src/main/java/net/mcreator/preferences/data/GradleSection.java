/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2023, Pylo, opensource contributors
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

package net.mcreator.preferences.data;

import com.sun.management.OperatingSystemMXBean;
import net.mcreator.preferences.PreferencesSection;
import net.mcreator.preferences.entries.BooleanEntry;
import net.mcreator.preferences.entries.IntegerEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;

public class GradleSection extends PreferencesSection {

	private static final Logger LOG = LogManager.getLogger(GradleSection.class);

	// Access to operating system memory statistics (JDK 14+ compatible interface)
	private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory
			.getOperatingSystemMXBean();

	/**
	 * Absolute maximum RAM available for the slider in preferences.
	 * Calculated based on TOTAL system memory.
	 * Logic: Total RAM - 1024MB (Reserving absolute minimum 1GB for OS stability).
	 */
	public static final int MAX_RAM = (int) (osBean.getTotalMemorySize() / 1048576) - 1024;

	/**
	 * Calculates a smart default RAM allocation.
	 * Logic:
	 * 1. If Low/Mid-end PC: Be careful, look at FREE memory.
	 * 2. If High-end PC (>12GB Total): Be aggressive, force at least 3GB, let OS
	 * handle swapping.
	 */
	private static int getSmartDefaultRam() {
		int totalRam = (int) (osBean.getTotalMemorySize() / 1048576);
		int freeRam = (int) (osBean.getFreeMemorySize() / 1048576);

		// Base calculation: Free RAM - 1.5GB buffer
		int targetRam = freeRam - 1536;

		LOG.info("Smart RAM Calc: Total: " + totalRam + "MB, Free: " + freeRam + "MB.");

		// --- POWER USER OVERRIDE ---
		// If the user has more than 12GB of TOTAL RAM, we assume the OS can handle
		// swapping.
		// We force a minimum of 3072MB (3GB) even if free RAM is currently low.
		if (totalRam > 12000) {
			if (targetRam < 3072) {
				LOG.info("High-Spec PC detected (>12GB). Overriding safety buffer. Forcing minimum 3GB.");
				targetRam = 3072;
			}
		}

		// Final Constraints
		if (targetRam > 4096)
			targetRam = 4096; // Cap default at 4GB
		if (targetRam < 1024)
			targetRam = 1024; // Absolute floor 1GB

		LOG.info("Smart RAM Decision: Setting default Xmx to: " + targetRam + "MB.");

		return targetRam;
	}

	public final BooleanEntry buildOnSave;
	public final BooleanEntry passLangToMinecraft;
	public final BooleanEntry enablePerformanceMonitor;
	public final IntegerEntry xmx;
	public final BooleanEntry offline;

	GradleSection(String preferencesIdentifier) {
		super(preferencesIdentifier);

		buildOnSave = addEntry(new BooleanEntry("buildOnSave", false));
		passLangToMinecraft = addEntry(new BooleanEntry("passLangToMinecraft", true));
		enablePerformanceMonitor = addEntry(new BooleanEntry("enablePerformanceMonitor", false));

		// Initialize Xmx with the smart calculated default.
		xmx = addEntry(new IntegerEntry("Xmx", Math.min(getSmartDefaultRam(), MAX_RAM), 128, MAX_RAM));

		offline = addEntry(new BooleanEntry("offline", false));
	}

	@Override
	public String getSectionKey() {
		return "gradle";
	}

}