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

import net.mcreator.generator.GeneratorConfiguration;
import net.mcreator.generator.GeneratorFlavor;
import net.mcreator.io.FileIO;
import net.mcreator.plugin.modapis.ModAPIImplementation;
import net.mcreator.plugin.modapis.ModAPIManager;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.workspace.Workspace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gradle.tooling.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradleUtils {

	private static final Logger LOG = LogManager.getLogger(GradleUtils.class);

	public static ProjectConnection getGradleProjectConnection(Workspace workspace) {
		updateMCreatorBuildFile(workspace); // update mcreator.gradle file if needed
		workspace.getGenerator().notifyDaemonsAboutChangedPaths(); // notify gradle daemons about changed paths
		return workspace.getGenerator().getGradleProjectConnection();
	}

	public static BuildActionExecuter<Void> getGradleSyncLauncher(GeneratorConfiguration generatorConfiguration,
			ProjectConnection projectConnection, String... additionalTasks) {
		BuildAction<Void> syncBuildAction = GradleSyncBuildAction.loadFromIsolatedClassLoader();
		BuildActionExecuter<Void> retval = projectConnection.action().projectsLoaded(syncBuildAction, unused -> {
		})
				.build().forTasks(additionalTasks);
		return configureLauncher(generatorConfiguration, retval); // make sure we have proper JVM, environment, ...
	}

	public static BuildLauncher getGradleTaskLauncher(GeneratorConfiguration generatorConfiguration,
			ProjectConnection projectConnection, String... tasks) {
		BuildLauncher retval = projectConnection.newBuild().forTasks(tasks);
		return configureLauncher(generatorConfiguration, retval); // make sure we have proper JVM, environment, ...
	}

	public static <T> ModelBuilder<T> getGradleModelBuilder(GeneratorConfiguration generatorConfiguration,
			ProjectConnection projectConnection, Class<T> clazz) {
		return configureLauncher(generatorConfiguration, projectConnection.model(clazz));
	}

	private static <T extends ConfigurableLauncher<T>> T configureLauncher(
			GeneratorConfiguration generatorConfiguration, T launcher) {

		int xmx = PreferencesManager.PREFERENCES.gradle.xmx.get();

		// For some unexplainable reason, ForgeGradle eclipse model import does not
		// generate
		// all files if Xmx is passed. Likely some bug with Forge Gradle daemons where
		// daemon
		// fails to operate correctly so new one needs to be created for model building
		if (!(launcher instanceof ModelBuilder<?>
				&& generatorConfiguration.getGeneratorFlavor() == GeneratorFlavor.FORGE))
			launcher.addJvmArguments("-Xmx" + xmx + "m");

		// --- OPTIMIZATIONS START ---

		// Always use G1GC (modern standard) and enable caching/daemon
		launcher.addJvmArguments("-XX:+UseG1GC");

		List<String> buildArgs = new ArrayList<>();
		buildArgs.add("-Dorg.gradle.caching=true");
		buildArgs.add("-Dorg.gradle.daemon=true");

		// Logic split: Low-end PC vs High-end PC optimization
		if (xmx < 1596) {
			LOG.info(
					"Gradle Optimizations: ECONOMY MODE enabled (< 1596MB). Parallel builds disabled, aggressive GC enabled.");
			// ECONOMY MODE: If less than 1596MB RAM is allocated
			// Enable aggressive string deduplication and fast cache clearing to save RAM
			launcher.addJvmArguments(
					"-XX:+UseStringDeduplication",
					"-XX:SoftRefLRUPolicyMSPerMB=50");
			// IMPORTANT: Do not enable parallel=true here, as multi-threaded builds consume
			// too much memory
		} else {
			LOG.info(
					"Gradle Optimizations: PERFORMANCE MODE enabled (>= 1596MB). Parallel builds and VFS watching enabled.");
			// PERFORMANCE MODE: If memory is sufficient (4GB+)
			// Enable parallel build and file system watching for speed
			buildArgs.add("-Dorg.gradle.parallel=true");
			buildArgs.add("-Dorg.gradle.vfs.watch=true");
		}

		// --- OPTIMIZATIONS END ---

		// make sure Gradle reports in English so our error decoder works properly
		launcher.addJvmArguments("-Duser.language=en");

		String java_home = getJavaHome();
		if (java_home != null) // make sure detected JAVA_HOME is not null
			launcher = launcher.setJavaHome(new File(java_home));

		// use custom set of environment variables to prevent system overrides
		launcher.setEnvironmentVariables(getEnvironment(java_home));

		if (java_home != null) {
			buildArgs.add("-Porg.gradle.java.installations.auto-detect=false");
			buildArgs.add("-Porg.gradle.java.installations.paths=" + java_home.replace('\\', '/'));
		}

		launcher.withArguments(buildArgs);

		// some mod API toolchains (NeoGradle, Mod Dev Gradle) need to think they are
		// running in IDE, so we make them think we are Eclipse
		launcher.addJvmArguments("-Declipse.application=net.mcreator");

		return launcher;
	}

	public static String getJavaHome() {
		// check if JAVA_HOME was overwritten in preferences and return this one in such
		// case
		if (PreferencesManager.PREFERENCES.hidden.java_home.get() != null
				&& PreferencesManager.PREFERENCES.hidden.java_home.get().isFile()) {
			LOG.warn("Using java home override specified by users!");
			String path = PreferencesManager.PREFERENCES.hidden.java_home.get().toString().replace("\\", "/");
			if (new File(path).exists() && path.contains("/bin/java"))
				return path.split("/bin/java")[0];
			else
				LOG.error("Java home override from preferences is not valid!");
		}

		// otherwise, we try to set JAVA_HOME to the same Java as MCreator is launched
		// with
		return System.getProperty("java.home");
	}

	public static void updateMCreatorBuildFile(Workspace workspace) {
		if (workspace != null) {
			StringBuilder mcreatorGradleConfBuilder = new StringBuilder();

			if (workspace.getWorkspaceSettings() != null
					&& workspace.getWorkspaceSettings().getMCreatorDependencies() != null) {
				for (String dep : workspace.getWorkspaceSettings().getMCreatorDependencies()) {
					ModAPIImplementation implementation = ModAPIManager.getModAPIForNameAndGenerator(dep,
							workspace.getGenerator().getGeneratorName());
					if (implementation != null) {
						mcreatorGradleConfBuilder.append(implementation.gradle()).append("\n\n");
					}
				}
			}

			FileIO.writeStringToFile(mcreatorGradleConfBuilder.toString(),
					new File(workspace.getWorkspaceFolder(), "mcreator.gradle"));
		}
	}

	public static Map<String, String> getEnvironment(String java_home) {
		Map<String, String> environment = new HashMap<>(System.getenv());

		// avoid global overrides
		cleanupEnvironment(environment);

		if (java_home != null)
			environment.put("JAVA_HOME", java_home);

		return environment;
	}

	public static void cleanupEnvironment(Map<String, String> environment) {
		// General Java environment variables
		environment.remove("JAVA_HOME");
		environment.remove("JRE_HOME");
		environment.remove("JDK_HOME");
		environment.remove("CLASSPATH");
		environment.remove("_JAVA_OPTIONS");
		environment.remove("JAVA_OPTS");
		environment.remove("JAVA_TOOL_OPTIONS");
		environment.remove("JAVACMD");
		environment.remove("JDK_JAVA_OPTIONS");

		// Gradle-specific environment variables
		environment.remove("GRADLE_HOME");
		environment.remove("GRADLE_OPTS");
		environment.remove("GRADLE_USER_HOME");
	}

	public static void clearGradleConfigurationCache(Workspace workspace) {
		File gradleCache = new File(workspace.getWorkspaceFolder(), ".gradle/configuration-cache");
		if (gradleCache.isDirectory()) {
			FileIO.deleteDir(gradleCache);
		}
	}

}
