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

package net.mcreator.io;

import net.mcreator.Launcher;
import io.sentry.ITransaction;
import io.sentry.ProfileLifecycle;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import net.mcreator.util.DefaultExceptionHandler;
import net.mcreator.util.LoggingOutputStream;
import net.mcreator.util.TestUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;

public class LoggingSystem {

	private static ITransaction rootTransaction;

	public static void init() {
		System.setProperty("log_directory", UserFolderManager.getFileFromUserFolder("").getAbsolutePath());

		// Manually check preferences for file logging before full initialization
		String logLevel = "OFF";
		try {
			java.io.File prefFile = UserFolderManager.getFileFromUserFolder("userpreferences");
			if (prefFile.isFile()) {
				// Simple check to avoid full JSON parsing overhead if possible, or just strict
				// check
				String content = new String(java.nio.file.Files.readAllBytes(prefFile.toPath()));
				// We look for "enableFileLogging": true
				// This is a naive check but effective for bootstrapping without loading all
				// prefs dependencies
				if (content.contains("\"enableFileLogging\": true")) {
					logLevel = "ALL";
				}
			}
		} catch (Exception e) {
			// Ignore errors, default to OFF
		}
		System.setProperty("log_file_level", logLevel);

		if (OS.getOS() == OS.WINDOWS && ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.noneMatch(arg -> arg.contains("idea_rt.jar"))) {
			System.setProperty("log_disable_ansi", "true");
		} else {
			System.setProperty("log_disable_ansi", "false");
		}

		// noinspection resource
		System.setErr(new PrintStream(
				new LoggingOutputStream(LogManager.getLogger("STDERR"), Level.ERROR).withCustomLogAction(log -> {
					// Fail tests if anything but JavaFX configuration error is logged to STDERR
					if (TestUtil.isTestingEnvironment() && !log.contains("Unsupported JavaFX configuration")) {
						TestUtil.failIfTestingEnvironment();
					}
				}), true));
		System.setOut(new PrintStream(new LoggingOutputStream(LogManager.getLogger("STDOUT"), Level.INFO), true));

		Sentry.init(options -> {
			options.setDsn(
					"https://c152e8bf2346827a81d9a21dba561a30@o4510923143512064.ingest.de.sentry.io/4510923157536848");
			options.setRelease(Launcher.version.getFullString());
			options.setEnvironment(Launcher.version.isDevelopment() ? "development" : "production");
			options.setTracesSampleRate(1.0);
			options.setSendDefaultPii(true);

			if (OS.getOS() == OS.WINDOWS) {
				options.setProfilesSampleRate(0.0);
			} else {
				options.setProfilesSampleRate(1.0);
				options.setProfileSessionSampleRate(1.0);
				options.setProfileLifecycle(ProfileLifecycle.TRACE);
			}

			options.setBeforeSend((event, hint) -> {
				if (event.getMessage() != null && event.getMessage().getFormatted() != null) {
					String msg = event.getMessage().getFormatted();
					// Ignore JavaFX startup warnings and other common "noisy" logs
					if (msg.contains("Unsupported JavaFX configuration") || msg.contains(
							"com.sun.javafx.application.PlatformImpl startup")
							|| msg.contains(
									"SLF4J: Defaulting to no-operation (NOP) logger implementation"))
						return null;

					// Ignore common non-critical network errors
					if (msg.contains("java.net.ConnectException") || msg.contains("java.net.UnknownHostException")
							|| msg.contains("java.net.SocketException"))
						return null;
				}

				if (event.getExceptions() != null) {
					for (io.sentry.protocol.SentryException ex : event.getExceptions()) {
						if (ex.getType() != null && (ex.getType().contains("ConnectException") || ex.getType().contains(
								"UnknownHostException") || ex.getType().contains("SocketException")))
							return null;
						if (ex.getValue() != null && (ex.getValue().contains("Unsupported JavaFX configuration")
								|| ex.getValue().contains("com.sun.javafx.application.PlatformImpl startup")))
							return null;
					}
				}

				return event;
			});
		});

		Sentry.configureScope(scope -> {
			scope.setTag("os.name", System.getProperty("os.name"));
			scope.setTag("os.version", System.getProperty("os.version"));
			scope.setTag("os.arch", System.getProperty("os.arch"));
			scope.setTag("java.version", System.getProperty("java.version"));
			scope.setTag("java.vendor", System.getProperty("java.vendor"));
			scope.setTag("cpu.cores", String.valueOf(Runtime.getRuntime().availableProcessors()));
			scope.setTag("memory.max", String.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB");
		});

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

		rootTransaction = Sentry.startTransaction("MCreator Session", "app.lifecycle");
	}

	public static void stop() {
		if (rootTransaction != null) {
			rootTransaction.finish(SpanStatus.OK);
			rootTransaction = null;
		}
	}

}
