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

package net.mcreator.ui.notifications;

import net.mcreator.plugin.PluginLoadFailure;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;

public class StartupNotifications {

	private static boolean notificationsHandled = false;

	public static <T extends Window & INotificationConsumer> void handleStartupNotifications(T parent) {
		if (!notificationsHandled) {
			Runnable action = () -> {
				// those show now dialogs initially, only notifications
				handleUpdatesPlugin(parent);
				handlePluginLoadFails(parent);
				handleWindowsDefenderExclusions(parent);

				// dialog if enabled, otherwise last in chain so this notification is on the top
				handleUpdatesCore(parent);
			};

			if (SwingUtilities.isEventDispatchThread()) {
				action.run();
			} else {
				try {
					SwingUtilities.invokeAndWait(action);
				} catch (InterruptedException | InvocationTargetException e) {
					action.run(); // Fallback just run it if waiting fails or interrupted
				}
			}

			notificationsHandled = true;
		}
	}

	private static <T extends Window & INotificationConsumer> void handleUpdatesCore(T parent) {
		// Updates disabled
	}

	private static <T extends Window & INotificationConsumer> void handleUpdatesPlugin(T parent) {
		// Plugin updates moved to separate UI or disabled
	}

	private static <T extends Window & INotificationConsumer> void handlePluginLoadFails(T parent) {
		Collection<PluginLoadFailure> failedPlugins = PluginLoader.INSTANCE.getFailedPlugins();

		if (!failedPlugins.isEmpty()) {
			parent.addNotification(UIRES.get("18px.warning"),
					L10N.t("notification.plugin_load_failed.msg") + "<br><p style='width:240px'><kbd>"
							+ failedPlugins.stream().map(PluginLoadFailure::pluginID).collect(Collectors.joining(", ")),
					new NotificationsRenderer.ActionButton(L10N.t("notification.common.more_info"), e -> {
						StringBuilder sb = new StringBuilder();
						sb.append("<html>");
						sb.append(L10N.t("dialog.plugin_load_failed.msg1"));
						sb.append("<ul>");
						for (PluginLoadFailure plugin : failedPlugins) {
							sb.append("<li><b>").append(plugin.pluginID()).append("</b> - reason: ")
									.append(StringUtils.abbreviateString(plugin.message(), 100, true))
									.append("<br><small>Location: ").append(plugin.pluginFile())
									.append("</small></li>");
						}
						sb.append("</ul><br>");
						sb.append(L10N.t("dialog.plugin_load_failed.msg2"));

						JOptionPane.showMessageDialog(parent, sb.toString(), L10N.t("dialog.plugin_load_failed.title"),
								JOptionPane.WARNING_MESSAGE);
					}));
		}
	}

	private static <T extends Window & INotificationConsumer> void handleWindowsDefenderExclusions(T parent) {
		if (net.mcreator.io.OS.getOS() != net.mcreator.io.OS.WINDOWS)
			return;

		// Check if we already asked
		if (PreferencesManager.PREFERENCES.ui.defenderExclusionAsked.get())
			return;

		// We do this check in background to not freeze UI
		new Thread(() -> {
			try {
				File gradleCache = net.mcreator.util.OfflineCacheManager.getOfflineCacheDir();
				if (!gradleCache.exists())
					return;

				String path = gradleCache.getAbsolutePath();
				ProcessBuilder pb = new ProcessBuilder("powershell", "-Command",
						"Get-MpPreference | Select-Object -ExpandProperty ExclusionPath");
				Process p = pb.start();
				String output = new String(p.getInputStream().readAllBytes());

				if (!output.contains(path)) {
					SwingUtilities.invokeLater(() -> {
						parent.addNotification(UIRES.get("18px.warning"),
								"Антивирус может замедлять работу программы.<br>Добавить MCreator в исключения?",
								new NotificationsRenderer.ActionButton("Добавить", e -> {
									try {
										String cmd = "Start-Process powershell -Verb RunAs -WindowStyle Hidden -ArgumentList 'Add-MpPreference -ExclusionPath \""
												+ path + "\"'";
										new ProcessBuilder("powershell", "-Command", cmd).start();
										PreferencesManager.PREFERENCES.ui.defenderExclusionAsked.set(true);
										PreferencesManager.savePreferences();
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}),
								new NotificationsRenderer.ActionButton("Не спрашивать", e -> {
									PreferencesManager.PREFERENCES.ui.defenderExclusionAsked.set(true);
									PreferencesManager.savePreferences();
								}));
					});
				}
			} catch (Exception e) {
				// Ignore if check fails
			}
		}).start();
	}

}
