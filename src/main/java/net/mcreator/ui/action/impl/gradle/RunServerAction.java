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

package net.mcreator.ui.action.impl.gradle;

import net.mcreator.minecraft.MinecraftOptionsUtils;
import net.mcreator.minecraft.ServerUtil;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.init.L10N;
import net.mcreator.util.DesktopUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class RunServerAction extends GradleAction {

	private static final Logger LOG = LogManager.getLogger("Run Minecraft Server");

	public RunServerAction(ActionRegistry actionRegistry) {
		super(actionRegistry, L10N.t("action.run_server_and_client"), null);
		setActionListener(evt -> {
			if (!ServerUtil.isEULAAccepted(actionRegistry.getMCreator().getWorkspace())) {
				ServerUtil.acceptEULA(actionRegistry.getMCreator().getWorkspace());
			}
			runServer();
		});
	}

	private void runServer() {
		actionRegistry.getMCreator().getGradleConsole()
				.markRunning(); // so console gets locked while we generate code already
		try {
			actionRegistry.getMCreator().getGenerator().runResourceSetupTasks();
			actionRegistry.getMCreator().getGenerator().generateBase();

			if (PreferencesManager.PREFERENCES.gradle.passLangToMinecraft.get())
				MinecraftOptionsUtils.setLangTo(actionRegistry.getMCreator().getWorkspace(), L10N.getLocaleString());

			String tasksToRun = actionRegistry.getMCreator().getGeneratorConfiguration().getGradleTaskFor("run_server");
			if (actionRegistry.getMCreator().getGeneratorConfiguration().getGradleTaskFor("run_client") != null) {
				tasksToRun +=
						" " + actionRegistry.getMCreator().getGeneratorConfiguration().getGradleTaskFor("run_client");
			}

			actionRegistry.getMCreator().getTabs().showTab(actionRegistry.getMCreator().consoleTab);
			actionRegistry.getMCreator().getGradleConsole().exec(tasksToRun);
		} catch (Exception e) { // if something fails, we still need to free the gradle console
			LOG.error("Failed to run server", e);
			actionRegistry.getMCreator().getGradleConsole().markReady();
		}
	}

	@Override public boolean isEnabled() {
		return actionRegistry.getMCreator().getGeneratorConfiguration().getGradleTaskFor("run_server") != null
				&& super.isEnabled();
	}
}
