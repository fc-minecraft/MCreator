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

package net.mcreator.ui.action.impl;

import net.mcreator.Launcher;
import net.mcreator.io.FileIO;
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.BasicAction;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.init.AppIcon;
import net.mcreator.ui.laf.themes.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class AboutAction extends BasicAction {

	public AboutAction(ActionRegistry actionRegistry) {
		super(actionRegistry, "О программе", evt -> showDialog(actionRegistry.getMCreator()));
	}

	public static void showDialog(Window parent) {
		JTabbedPane tabbedPane = new JTabbedPane();

		// --- Tab 1: About ---
		JPanel logoPanel = new JPanel(new BorderLayout(24, 24));
		logoPanel.add("North", new JLabel(AppIcon.getAppIcon(128, 128)));
		logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

		String message = "MCreator FunCode Edition\n\n" +
				"Версия: " + Launcher.version.getFullString() + "\n" +
				"Разработано для обучения программированию.\n\n" +
				"Сборка и адаптация специально для FunCode.\n" +
				"Разработка FunCode версии: Nikita Gutsenkov\n\n" +
				"© FunCode. Все авторские права защищены.\n" +
				"Продукт защищен технологией DRM (Digital Rights Management).\n" +
				"Любое несанкционированное копирование, распространение\n" +
				"или модификация данного ПО строго запрещены.";

		JTextArea aboutLabel = new JTextArea(message);
		aboutLabel.setEditable(false);
		aboutLabel.setOpaque(false);
		aboutLabel.setFont(Theme.current().getFont().deriveFont(14f));

		String sysInfoText = getSystemInfo();

		JTextArea sysInfoArea = new JTextArea(sysInfoText);
		sysInfoArea.setEditable(false);
		sysInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		sysInfoArea.setCaretPosition(0);

		JScrollPane sysInfoScroll = new JScrollPane(sysInfoArea);
		sysInfoScroll.setPreferredSize(new Dimension(500, 150));
		sysInfoScroll.setBorder(BorderFactory.createTitledBorder("Техническая информация"));

		JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
		contentPanel.add(aboutLabel, BorderLayout.NORTH);
		contentPanel.add(sysInfoScroll, BorderLayout.CENTER);

		JComponent aboutPanel = PanelUtils.westAndCenterElement(
				PanelUtils.pullElementUp(PanelUtils.centerInPanel(logoPanel)), contentPanel, 48, 48);
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 32));

		tabbedPane.addTab("О программе", aboutPanel);

		// --- Tab 2: Licenses ---
		StringBuilder licenseText = new StringBuilder();
		licenseText.append("MCreator is licensed under GPL v3.0\n\n");

		File licenseFile = new File("LICENSE.txt");
		if (licenseFile.exists()) {
			licenseText.append(FileIO.readFileToString(licenseFile));
		} else {
			licenseText.append("License file not found.");
		}

		licenseText.append("\n\n-------------------------------\n");
		licenseText.append("Third party licenses provided in 'license' folder:\n\n");

		File licenseDir = new File("license");
		if (licenseDir.isDirectory()) {
			File[] files = licenseDir.listFiles();
			if (files != null) {
				Arrays.stream(files)
						.sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
						.forEach(f -> {
							licenseText.append("=================================================================\n");
							licenseText.append("LICENSE: ").append(f.getName()).append("\n");
							licenseText.append("=================================================================\n\n");
							licenseText.append(FileIO.readFileToString(f)).append("\n\n");
						});
			}
		}

		JTextArea licenseArea = new JTextArea(licenseText.toString());
		licenseArea.setEditable(false);
		licenseArea.setFont(Theme.current().getFont().deriveFont(12f));
		// Use monospaced font for license text if possible, but theme font is cleaner.
		// licenseArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		JScrollPane scrollPane = new JScrollPane(licenseArea);
		scrollPane.setPreferredSize(new Dimension(700, 500));
		// Initial scroll to top
		licenseArea.setCaretPosition(0);

		tabbedPane.addTab("Лицензии", scrollPane);

		// Show Dialog
		Object[] options = { "Закрыть" };
		JOptionPane.showOptionDialog(parent, tabbedPane, "О программе",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}

	public static String getSystemInfo() {
		long maxMem = Runtime.getRuntime().maxMemory() / 1048576;
		long totalMem = Runtime.getRuntime().totalMemory() / 1048576;
		long freeMem = Runtime.getRuntime().freeMemory() / 1048576;
		long usedMem = totalMem - freeMem;

		StringBuilder sysInfo = new StringBuilder();
		try {
			sysInfo.append("=== Сборка ПО ===\n");
			sysInfo.append("MCreator Версия: ").append(Launcher.version.getFullString()).append("\n");

			String buildDate = "Неизвестно";
			try {
				java.util.Enumeration<java.net.URL> resources = Launcher.class.getClassLoader()
						.getResources("META-INF/MANIFEST.MF");
				while (resources.hasMoreElements()) {
					java.util.jar.Manifest manifest = new java.util.jar.Manifest(resources.nextElement().openStream());
					String bd = manifest.getMainAttributes().getValue("Build-Date");
					if (bd != null && manifest.getMainAttributes().getValue("MCreator-Version") != null) {
						buildDate = bd;
						break;
					}
				}
			} catch (Exception ignored) {
			}
			sysInfo.append("Дата сборки: ").append(buildDate).append("\n");
			sysInfo.append("Snapshot: ").append(Launcher.version.isSnapshot()).append("\n");
			sysInfo.append("Development: ").append(Launcher.version.isDevelopment()).append("\n");
			sysInfo.append("Хэш версии: ").append(Launcher.version.versionlong).append("\n\n");
		} catch (Exception e) {
		}

		try {
			sysInfo.append("=== Системная информация ===\n");
			sysInfo.append("ОС: ").append(System.getProperty("os.name", "Unknown")).append(" ")
					.append(System.getProperty("os.version", "Unknown"))
					.append(" (").append(System.getProperty("os.arch", "Unknown")).append(")\n");
			sysInfo.append("Пользователь: ").append(System.getProperty("user.name", "Unknown")).append("\n");
			sysInfo.append("Каталог пользователя: ").append(System.getProperty("user.home", "Unknown")).append("\n");
			sysInfo.append("Рабочая директория: ").append(System.getProperty("user.dir", "Unknown")).append("\n");
			sysInfo.append("Разрядность системы данных: ").append(System.getProperty("sun.arch.data.model", "Unknown"))
					.append("-bit\n");
			sysInfo.append("Локаль: ").append(Locale.getDefault().toLanguageTag()).append("\n");
			sysInfo.append("Часовой пояс: ").append(TimeZone.getDefault().getID()).append("\n");
			sysInfo.append("Кодировка файлов: ").append(System.getProperty("file.encoding", "Unknown")).append("\n\n");
		} catch (Exception e) {
			sysInfo.append("Ошибка чтения свойств ОС.\n\n");
		}

		sysInfo.append("=== Оборудование ===\n");
		try {
			sysInfo.append("Процессоры (логические): ").append(Runtime.getRuntime().availableProcessors()).append("\n");
		} catch (Exception e) {
		}

		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			sysInfo.append("Мониторы:\n");
			for (GraphicsDevice gd : ge.getScreenDevices()) {
				DisplayMode dm = gd.getDisplayMode();
				sysInfo.append("  - ").append(dm.getWidth()).append("x").append(dm.getHeight())
						.append(" @ ").append(dm.getRefreshRate()).append("Hz (")
						.append(dm.getBitDepth()).append("-bit)\n");
			}
		} catch (Throwable e) {
			sysInfo.append("  <Графика недоступна>\n");
		}

		try {
			sysInfo.append("Диски (Учтены только доступные):\n");
			for (File root : File.listRoots()) {
				long t = root.getTotalSpace();
				if (t > 0) {
					long f = root.getFreeSpace();
					sysInfo.append("  [").append(root.getAbsolutePath()).append("] Свободно ")
							.append(f / 1073741824L).append(" GB из ")
							.append(t / 1073741824L).append(" GB\n");
				}
			}
			sysInfo.append("\n");
		} catch (Throwable e) {
			sysInfo.append("  <Информация о дисках недоступна>\n\n");
		}

		sysInfo.append("=== Системные ресурсы Java ===\n");
		try {
			sysInfo.append("Heap Память: Использовано ").append(usedMem).append(" MB / Доступно ")
					.append(maxMem).append(" MB\n");
		} catch (Exception e) {
		}

		try {
			MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
			sysInfo.append("Non-Heap Память (Использовано): ")
					.append(memBean.getNonHeapMemoryUsage().getUsed() / 1048576).append(" MB\n");
		} catch (Throwable e) {
		}

		try {
			RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
			sysInfo.append("Время работы (Uptime): ").append(runtimeBean.getUptime() / 1000).append(" сек\n");
		} catch (Throwable e) {
		}

		try {
			ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
			sysInfo.append("Активные потоки: ").append(threadBean.getThreadCount())
					.append(" (Пик: ").append(threadBean.getPeakThreadCount()).append(")\n");
		} catch (Throwable e) {
		}

		try {
			ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
			sysInfo.append("Загружено классов: ").append(classBean.getLoadedClassCount())
					.append(" (Всего за сессию: ").append(classBean.getTotalLoadedClassCount()).append(")\n\n");
		} catch (Throwable e) {
			sysInfo.append("\n");
		}

		try {
			sysInfo.append("=== Окружение Java ===\n");
			sysInfo.append("Версия Java: ").append(System.getProperty("java.version", "Unknown")).append("\n");
			sysInfo.append("Поставщик Java: ").append(System.getProperty("java.vendor", "Unknown")).append("\n");
			sysInfo.append("VM: ").append(System.getProperty("java.vm.name", "Unknown")).append(" (")
					.append(System.getProperty("java.vm.version", "Unknown")).append(")\n");
			sysInfo.append("Домашняя директория Java: ").append(System.getProperty("java.home", "Unknown"))
					.append("\n");
			sysInfo.append("Версия класса: ").append(System.getProperty("java.class.version", "Unknown"))
					.append("\n\n");
		} catch (Exception e) {
			sysInfo.append("Ошибка окружения Java.\n\n");
		}

		try {
			sysInfo.append("=== Аргументы JVM ===\n");
			RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
			java.util.List<String> jvmArgs = runtimeBean.getInputArguments();
			for (String arg : jvmArgs) {
				sysInfo.append(arg).append("\n");
			}
		} catch (Throwable e) {
			sysInfo.append("<Недоступно>\n");
		}

		return sysInfo.toString();
	}
}
