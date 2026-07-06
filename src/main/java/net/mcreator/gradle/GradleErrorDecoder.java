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

package net.mcreator.gradle;

import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.gradle.GradleErrorDialogs;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleErrorDecoder {

	/**
	 * This method tries to decode task result based on given parameters, returns status code and shows error message if error is detected
	 *
	 * @param out         String containing data from out stream
	 * @param err         String containing data from err stream
	 * @param whereToShow Parent window on which to show the error dialog
	 * @return One of GradleTaskResult status codes, STATUS_UNKNOWN if GradleErrorDecoder can't decide the type of error
	 */
	public static GradleResultCode processErrorAndShowMessage(String out, String err, MCreator whereToShow) {
		// normalize spaces
		out = out.replace('\u00a0', ' ');
		err = err.replace('\u00a0', ' ');

		if (err.contains("\nExecution failed for task ':reobfJar'")) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_REOBF_FAILED, whereToShow);
		}

		//check if there is no internet or the connection is blocked by a firewall
		if (err.contains(" Software caused connection abort: ") && out.contains("\nBUILD FAILED\n")) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_INTERNET_INTERRUPTED, whereToShow);
		}

		//check if there is no internet or the connection is blocked by a firewall
		if ((err.contains("Could not GET ") && err.contains("Could not resolve ")) || (
				err.contains(" Network is unreachable: ") && err.contains("Could not resolve ")) || (
				err.contains("Could not HEAD ") && err.contains("Could not resolve "))) {

			return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_NO_INTERNET, whereToShow);
		}

		//Check if cache files are corrupt
		if ((err.contains("java.io.FileNotFoundException: ") && err.contains("McpMappings.json (")) || (
				err.contains("Could not open proj remapped class cache for ") && err.contains(
						"java.io.FileNotFoundException: "))) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_CACHEDATA_ERROR, whereToShow);
		}

		//Check if cache files are outdated
		if ((err.contains("No cached version of ") && err.contains(" available for offline mode.")) || (
				err.contains(" not found! Maybe you are running in offline mode?") && err.contains(
						"java.io.FileNotFoundException"))) {
			if (PreferencesManager.PREFERENCES.gradle.offline.get())
				return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_CACHEDATA_OUTDATED, whereToShow);
			else
				return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_CACHEDATA_ERROR, whereToShow);
		}

		//Check if JVM ran out of RAM
		if (err.contains("java.lang.OutOfMemoryError: Java heap space") || out.contains(
				"java.lang.OutOfMemoryError: Java heap space") || err.contains("Could not reserve enough space for")
				|| out.contains("Could not reserve enough space for") || err.contains("GC overhead limit exceeded") || (
				err.contains("Execution failed for task") && out.contains(
						"Daemon stopping because JVM tenured space is exhausted"))) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.JAVA_JVM_HEAP_SPACE, whereToShow);
		}

		//Check if XMX parameter was set to a wrong value
		if (err.contains("Invalid maximum heap size:")) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.JAVA_XMX_INVALID_VALUE, whereToShow);
		}

		//Check if XMS parameter was set to a wrong value
		if (err.contains("Invalid initial heap size:") || err.contains(
				"Initial heap size set to a larger value than the maximum heap size")) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.JAVA_XMS_INVALID_VALUE, whereToShow);
		}

		//check if the error was caused by JVM crash and no other errors are present
		if ((out.contains("The crash happened outside the Java Virtual Machine in native code") || err.contains(
				"The crash happened outside the Java Virtual Machine in native code")) && (
				out.contains("A fatal error has been detected by the Java Runtime Environment") || err.contains(
						"A fatal error has been detected by the Java Runtime Environment"))) {

			return GradleErrorDialogs.showErrorDialog(GradleResultCode.JAVA_JVM_CRASH_ERROR, whereToShow);

		}

		// Check for runtime Minecraft model loading errors (visible in game log)
		checkRuntimeModelErrors(out, whereToShow);

		// check if the gameplay crashed, we do not do anything in such cases
		if (out.contains("Task :runClient FAILED") || out.contains("Execution failed for task ':runClient'")) {
			return GradleResultCode.JAVA_RUN_CRASHED;
		}

		//if we don't know why, but the build fails, we report GRADLE_BUILD_FAILED
		if (out.contains("\nBUILD FAILED")) {
			return GradleErrorDialogs.showErrorDialog(GradleResultCode.GRADLE_BUILD_FAILED, whereToShow);
		}

		//if no error is detected, we return STATUS_OK
		return GradleResultCode.STATUS_OK;
	}

	/**
	 * Checks for runtime Minecraft model loading errors in the game log output and shows a warning.
	 * Called both on successful and failed game runs.
	 */
	public static void checkRuntimeModelErrors(String out, MCreator whereToShow) {
		if (out.contains("Failed to load model") || out.contains("Missing block model")
				|| out.contains("Missing axis, expected to find a string")) {
			String modelName = extractModelName(out);
			String msg = "<html><b>Minecraft не смог загрузить одну или несколько 3D-моделей.</b><br><br>"
					+ (modelName != null ? "Проблемная модель: <b>" + modelName + "</b><br><br>" : "")
					+ "<b>Что делать:</b><br>"
					+ "— Проверь JSON-модели в разделе <b>Ресурсы → Модели</b><br>"
					+ "— Убедись, что в модели нет вращений по нескольким осям одновременно<br>"
					+ "— Экспортируй модель заново из Blockbench";
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(whereToShow, msg,
					"Ошибка загрузки модели", JOptionPane.WARNING_MESSAGE));
		}
	}

	public static boolean isErrorCausedByCorruptedCaches(String errortext) {
		errortext = errortext.replace('\u00a0', ' '); // normalize spaces
		if (!errortext.contains("Could not GET ") && !errortext.contains("Could not HEAD ") && !errortext.contains(
				"Network is unreachable:")) { // eliminate networking problems first
			if (errortext.contains("java.util.zip.ZipException: error in opening zip file")) {
				return true;
			}
			if (errortext.contains("UncheckedIOException: Could not load properties for module")) {
				return true;
			}
			return errortext.contains("Could not resolve all files for configuration");
		}
		return false;
	}

	public static boolean doesErrorSuggestRerun(String errortext) {
		errortext = errortext.replace('\u00a0', ' '); // normalize spaces
		return errortext.contains("try running the task again.");
	}

	public static boolean isErrorDueToJMXPortIssues(String errortext) {
		errortext = errortext.replace('\u00a0', ' '); // normalize spaces
		return errortext.contains("java.rmi.server.ExportException: Port already in use:") && errortext.contains(
				".jmxremote.");
	}

	/**
	 * Tries to extract the first model identifier from Minecraft log lines like:
	 * "Failed to load model exampleproject:models/custom/stickexp.json"
	 * or "Missing block model: exampleproject:block/tent"
	 */
	private static String extractModelName(String out) {
		Matcher m1 = Pattern.compile("Failed to load model ([^\\s\\n]+)").matcher(out);
		if (m1.find()) return m1.group(1);
		Matcher m2 = Pattern.compile("Missing block model: ([^\\s\\n\\]]+)").matcher(out);
		if (m2.find()) return m2.group(1);
		return null;
	}

}
