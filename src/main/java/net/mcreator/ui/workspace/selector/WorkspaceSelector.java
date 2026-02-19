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

package net.mcreator.ui.workspace.selector;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import net.mcreator.Launcher;
import net.mcreator.io.FileIO;
import net.mcreator.io.OS;
import net.mcreator.io.UserFolderManager;
import net.mcreator.plugin.MCREvent;
import net.mcreator.plugin.events.WorkspaceSelectorLoadedEvent;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.action.impl.AboutAction;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.file.FileDialogs;
import net.mcreator.ui.dialogs.preferences.PreferencesDialog;
import net.mcreator.ui.dialogs.workspace.NewWorkspaceDialog;
import net.mcreator.ui.init.AppIcon;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.ui.notifications.INotificationConsumer;
import net.mcreator.ui.notifications.NotificationsRenderer;
import net.mcreator.util.DesktopUtils;
import net.mcreator.util.ListUtils;
import net.mcreator.util.image.EmptyIcon;
import net.mcreator.util.image.ImageUtils;
import net.mcreator.workspace.ShareableZIPManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class WorkspaceSelector extends JFrame implements DropTargetListener, INotificationConsumer {

	private static final Logger LOG = LogManager.getLogger("Workspace Selector");

	private final CardLayout recentPanes = new CardLayout();
	private final JPanel recentPanel = new JPanel(recentPanes);
	private final WorkspaceOpenListener workspaceOpenListener;
	private RecentWorkspaces recentWorkspaces = new RecentWorkspaces();

	private boolean purged = false;

	@Nullable
	private final MCreatorApplication application;

	private final JButton newWorkspace;

	private final JPanel subactions = new JPanel(new GridLayout(-1, 1, 0, 10)); // Increased gap

	private final NotificationsRenderer notificationsRenderer;

	private final DefaultListModel<RecentWorkspaceEntry> defaultListModel = new DefaultListModel<>();
	private final JList<RecentWorkspaceEntry> recentsList = new JList<>(defaultListModel);
	private final JPopupMenu recentListPopupMenu;

	private JLabel drmStatusLabel;

	public void refreshDRMStatus() {
		if (drmStatusLabel != null) {
			long days = net.mcreator.ui.init.DRMAuthManager.getDaysRemaining();
			drmStatusLabel.setText("Лицензия: " + days + " дн.");
		}
	}

	public WorkspaceSelector(@Nullable MCreatorApplication application, WorkspaceOpenListener workspaceOpenListener) {
		this.workspaceOpenListener = workspaceOpenListener;
		this.application = application;

		setTitle("MCreator " + Launcher.version.getMajorString());
		setIconImages(AppIcon.getAppIcons());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		if (application != null)
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					application.closeApplication();
				}
			});

		JPanel actions = new JPanel(new BorderLayout(0, 15)); // Increased gap

		newWorkspace = mainWorkspaceButton(L10N.t("dialog.workspace_selector.new_workspace"), UIRES.get("wrk_add"),
				e -> {
					NewWorkspaceDialog newWorkspaceDialog = new NewWorkspaceDialog(this);
					if (newWorkspaceDialog.getWorkspaceFile() != null)
						workspaceOpenListener.workspaceOpened(newWorkspaceDialog.getWorkspaceFile());
				});

		actions.add("North", newWorkspace);
		actions.add("Center", subactions);

		addWorkspaceButton(L10N.t("dialog.workspace_selector.open_workspace"), UIRES.get("opnwrk"), e -> {
			File workspaceFile = FileDialogs.getOpenDialog(this, new String[] { ".mcreator" });
			if (workspaceFile != null && workspaceFile.getParentFile().isDirectory())
				workspaceOpenListener.workspaceOpened(workspaceFile);
		});

		addWorkspaceButton(L10N.t("dialog.workspace_selector.import"), UIRES.get("impfile"), e -> {
			File file = FileDialogs.getOpenDialog(this, new String[] { ".zip" });
			if (file != null) {
				File workspaceDir = FileDialogs.getWorkspaceDirectorySelectDialog(this, null);
				if (workspaceDir != null) {
					ShareableZIPManager.ImportResult workspaceFile = ShareableZIPManager.importZIP(file, workspaceDir,
							this);
					if (workspaceFile != null)
						workspaceOpenListener.workspaceOpened(workspaceFile.file(), workspaceFile.regenerateRequired());
				}
			}
		});

		JPanel logoPanel = new JPanel(new BorderLayout(5, 5));
		JLabel logo = new JLabel(UIRES.SVG.getBuiltIn("logo", 250, (int) (250 * (63 / 350.0))));
		// Removed logo link
		logoPanel.add("North", logo);

		logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

		JPanel southcenter = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		// Removed Donate button

		JLabel prefs = new JLabel(L10N.t("dialog.workspace_selector.preferences"));
		prefs.setIcon(UIRES.get("settings"));
		prefs.setCursor(new Cursor(Cursor.HAND_CURSOR));
		ComponentUtils.deriveFont(prefs, 15); // Increased font size
		prefs.setForeground(Theme.current().getForegroundColor());
		prefs.setBorder(BorderFactory.createEmptyBorder());
		prefs.setHorizontalTextPosition(JLabel.LEFT);
		prefs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				new PreferencesDialog(WorkspaceSelector.this, null);
			}
		});
		southcenter.add(prefs);

		JPanel southcenterleft = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel version = L10N.label("dialog.workspace_selector.version",
				Launcher.version.isSnapshot() ? Launcher.version.getMajorString() : Launcher.version.getFullString());
		version.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				AboutAction.showDialog(WorkspaceSelector.this);
			}
		});
		ComponentUtils.deriveFont(version, 15); // Increased font size
		version.setForeground(Theme.current().getForegroundColor());
		version.setHorizontalTextPosition(SwingConstants.LEFT);
		version.setIcon(UIRES.get("info"));
		version.setCursor(new Cursor(Cursor.HAND_CURSOR));
		southcenterleft.add(version);

		JPanel southSubComponent = new JPanel(new GridBagLayout());
		southSubComponent.setOpaque(false);
		southSubComponent.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.VERTICAL;

		// Left: Version
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		southSubComponent.add(southcenterleft, gbc);

		// Center: Button (if visible)
		boolean showCompleteSetup = !net.mcreator.util.OfflineCacheManager.isOfflineModeReady();
		boolean debugShowSetup = false; // TOGGLE

		if (showCompleteSetup || debugShowSetup) {
			// Clean design: Standard Label, Bold, Red, Pulsing Color
			JLabel completeSetup = new JLabel("ЗАВЕРШИТЬ НАСТРОЙКУ");
			completeSetup.setCursor(new Cursor(Cursor.HAND_CURSOR));
			// Use Bold font to stand out, but keep same size to avoid displacement
			completeSetup.setFont(completeSetup.getFont().deriveFont(Font.BOLD));
			completeSetup.setOpaque(false);

			// Initial Color
			completeSetup.setForeground(new Color(255, 60, 60)); // Base Red
			completeSetup.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

			if (showCompleteSetup) {
				Timer glowTimer = new Timer(50, new ActionListener() { // Faster 50ms for smoother animation
					float phase = 0f;

					@Override
					public void actionPerformed(ActionEvent e) {
						if (!isDisplayable()) {
							((Timer) e.getSource()).stop();
							return;
						}
						phase += 0.15f;
						// Pulse between Red and White-Red
						// R = 255
						// G, B = oscillates 0 to 180 (Whiteish)
						int gb = (int) (90 + 90 * Math.sin(phase));
						// Clamp just in case
						gb = Math.max(0, Math.min(255, gb));

						completeSetup.setForeground(new Color(255, gb, gb));
					}
				});
				glowTimer.start();
			}

			completeSetup.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					new PreferencesDialog(WorkspaceSelector.this, "Офлайн режим", true);
				}
			});

			gbc.gridx = 1;
			gbc.weightx = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(0, 0, 0, 0); // Reset insets

			// Wrap in standard FlowLayout (vgap=5) to match southcenterleft (Version)
			JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
			wrapper.setOpaque(false);
			wrapper.add(completeSetup);

			southSubComponent.add(wrapper, gbc);
		}

		// Right: Settings
		gbc.gridx = 2;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.EAST;

		// DRM Status & Logout
		long days = net.mcreator.ui.init.DRMAuthManager.getDaysRemaining();
		drmStatusLabel = new JLabel("Лицензия: " + days + " дн.");
		drmStatusLabel.setForeground(Theme.current().getForegroundColor());
		ComponentUtils.deriveFont(drmStatusLabel, 14);

		JButton logoutBtn = new JButton("Выйти");
		ComponentUtils.deriveFont(logoutBtn, 14);
		logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		logoutBtn.addActionListener(e -> {
			net.mcreator.ui.init.DRMAuthManager.logout();
			// Restart application to show login dialog again
			application.closeApplication();
		});

		// Add DRM info before the preferences button (which is already in southcenter)
		// Result: [Status] [Logout] [Settings] (Right aligned)
		southcenter.add(drmStatusLabel, 0);
		southcenter.add(logoutBtn, 1);

		southSubComponent.add(southcenter, gbc);

		JComponent centerComponent = PanelUtils.centerAndSouthElement(
				PanelUtils.northAndCenterElement(logoPanel, PanelUtils.totalCenterInPanel(actions)), southSubComponent);

		notificationsRenderer = new NotificationsRenderer(centerComponent);

		add("Center", centerComponent);

		recentPanel.setBackground(Theme.current().getSecondAltBackgroundColor());
		recentPanel.setPreferredSize(new Dimension(225, 10));

		JLabel norecentsloaded = L10N.label("dialog.workspace_selector.no_workspaces_loaded");
		norecentsloaded.setForeground(Theme.current().getAltForegroundColor());
		ComponentUtils.deriveFont(norecentsloaded, 14); // Increased font size
		JLabel norecents = L10N.label("dialog.workspace_selector.no_workspaces");
		norecents.setForeground(Theme.current().getAltForegroundColor());
		ComponentUtils.deriveFont(norecents, 14); // Increased font size

		recentsList.setComponentPopupMenu(recentListPopupMenu = buildRightClickMenu());

		recentsList.setBackground(Theme.current().getSecondAltBackgroundColor());
		recentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		recentsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
					int idx = recentsList.locationToIndex(mouseEvent.getPoint());
					removeRecentWorkspace(defaultListModel.elementAt(idx));
					reloadRecents();
				} else if (mouseEvent.getClickCount() == 2) {
					workspaceOpenListener.workspaceOpened(recentsList.getSelectedValue().getPath());
				}
			}
		});
		recentsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					Object[] options = { L10N.t("dialog.workspace_selector.delete_workspace.recent_list"),
							L10N.t("dialog.workspace_selector.delete_workspace.workspace"), L10N.t("common.cancel") };
					int n = JOptionPane.showOptionDialog(WorkspaceSelector.this,
							L10N.t("dialog.workspace_selector.delete_workspace.message",
									recentsList.getSelectedValue().getName()),
							L10N.t("dialog.workspace_selector.delete_workspace.title"),
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

					if (n == 0) {
						removeRecentWorkspace(recentsList.getSelectedValue());
						reloadRecents();
					} else if (n == 1) {
						int m = JOptionPane.showConfirmDialog(WorkspaceSelector.this,
								L10N.t("dialog.workspace_selector.delete_workspace.confirmation",
										recentsList.getSelectedValue().getName()),
								L10N.t("common.confirmation"),
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (m == JOptionPane.YES_OPTION) {
							FileIO.moveToTrash(recentsList.getSelectedValue().getPath().getParentFile());
							reloadRecents();
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					workspaceOpenListener.workspaceOpened(recentsList.getSelectedValue().getPath());
				}
			}
		});
		recentsList.setCellRenderer(new RecentWorkspacesRenderer());
		JScrollPane recentsScrollPane = new JScrollPane(recentsList);
		recentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		recentPanel.add("recents", recentsScrollPane);
		recentPanel.add("norecentsloaded", PanelUtils.totalCenterInPanel(norecentsloaded));
		recentPanel.add("norecents", PanelUtils.totalCenterInPanel(norecents));

		// initWebsitePanel(); // Removed website panel

		add("West", recentPanel);

		new DropTarget(this, DnDConstants.ACTION_MOVE, this, true, null);

		MCREvent.event(new WorkspaceSelectorLoadedEvent(this));

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				super.windowActivated(e);
				reloadRecents();
				if (!purged) {
					runAutoPurge();
					purged = true;
				}
				newWorkspace.requestFocusInWindow();
			}
		});

		setSize(795, 460);
		setResizable(false);
		setLocationRelativeTo(null);

		if (OS.getOS() == OS.WINDOWS) {
			getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
			centerComponent.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
		} else if (OS.getOS() == OS.MAC && SystemInfo.isMacFullWindowContentSupported) {
			getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
			getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
			getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
			recentPanel.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
			centerComponent.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
		}
	}

	public JPopupMenu getRecentListPopupMenu() {
		return recentListPopupMenu;
	}

	private JPopupMenu buildRightClickMenu() {
		JPopupMenu recentListMenu = new JPopupMenu();

		JMenuItem openSelectedWorkspace = new JMenuItem(L10N.t("dialog.workspace_selector.open_workspace_selected"));
		openSelectedWorkspace.addActionListener(a -> {
			if (recentsList.getSelectedValue() != null) {
				workspaceOpenListener.workspaceOpened(recentsList.getSelectedValue().getPath());
			}
		});
		recentListMenu.add(openSelectedWorkspace);

		recentListMenu.addSeparator();

		JMenuItem deleteFromRecentList = new JMenuItem(
				L10N.t("dialog.workspace_selector.delete_workspace.recent_list"));
		deleteFromRecentList.addActionListener(a -> {
			if (recentsList.getSelectedValue() != null) {
				removeRecentWorkspace(recentsList.getSelectedValue());
				reloadRecents();
			}
		});
		recentListMenu.add(deleteFromRecentList);

		JMenuItem deleteFromFolder = new JMenuItem(L10N.t("dialog.workspace_selector.delete_workspace.workspace"));
		deleteFromFolder.addActionListener(a -> {
			if (recentsList.getSelectedValue() != null) {
				int m = JOptionPane.showConfirmDialog(WorkspaceSelector.this,
						L10N.t("dialog.workspace_selector.delete_workspace.confirmation",
								recentsList.getSelectedValue().getName()),
						L10N.t("common.confirmation"),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (m == 0) {
					FileIO.moveToTrash(recentsList.getSelectedValue().getPath().getParentFile());
					reloadRecents();
				}
			}
		});
		recentListMenu.add(deleteFromFolder);

		recentListMenu.addSeparator();

		JMenuItem openInSystemExplorer = new JMenuItem(L10N.t("action.open_workspace_folder"));
		openInSystemExplorer.addActionListener(a -> {
			if (recentsList.getSelectedValue() != null) {
				DesktopUtils.openSafe(recentsList.getSelectedValue().getPath().getParentFile());
			}
		});
		recentListMenu.add(openInSystemExplorer);

		recentListMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				recentsList.setSelectedIndex(recentsList.locationToIndex(recentsList.getMousePosition()));
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});

		return recentListMenu;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		processDrag(dtde);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		processDrag(dtde);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dtde) {
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		Transferable transferable = dtde.getTransferable();
		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrop(dtde.getDropAction());
			try {
				List<?> transferData = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				if (!transferData.isEmpty()) {
					Object transfObj = transferData.getFirst();
					if (transfObj instanceof File workspaceFile) {
						if (workspaceFile.getName().endsWith(".mcreator")) {
							workspaceOpenListener.workspaceOpened(workspaceFile);
						} else {
							Toolkit.getDefaultToolkit().beep();
						}
					}
				}
			} catch (Exception ex) {
				LOG.error("Drag and drop failed", ex);
			}
		} else {
			dtde.rejectDrop();
		}
	}

	private void processDrag(DropTargetDragEvent dtde) {
		if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		} else {
			dtde.rejectDrag();
		}
	}

	public void addOrUpdateRecentWorkspace(RecentWorkspaceEntry recentWorkspaceEntry) {
		if (!recentWorkspaces.getList().contains(recentWorkspaceEntry))
			recentWorkspaces.getList().add(recentWorkspaceEntry);
		else
			recentWorkspaces.getList().get(recentWorkspaces.getList().indexOf(recentWorkspaceEntry))
					.update(recentWorkspaceEntry);

		ListUtils.rearrange(recentWorkspaces.getList(), recentWorkspaceEntry);
		saveRecentWorkspaces();
	}

	private void removeRecentWorkspace(RecentWorkspaceEntry recentWorkspace) {
		recentWorkspaces.getList().remove(recentWorkspace);
		saveRecentWorkspaces();
	}

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().setStrictness(Strictness.LENIENT).create();

	private void saveRecentWorkspaces() {
		String serialized = gson.toJson(recentWorkspaces);
		if (serialized != null && !serialized.isEmpty()) {
			FileIO.writeStringToFile(serialized, UserFolderManager.getFileFromUserFolder("recentworkspaces"));
		}
	}

	private synchronized void reloadRecents() {
		if (UserFolderManager.getFileFromUserFolder("recentworkspaces").isFile()) {
			try {
				recentWorkspaces = gson.fromJson(
						FileIO.readFileToString(UserFolderManager.getFileFromUserFolder("recentworkspaces")),
						RecentWorkspaces.class);
				if (recentWorkspaces != null) {
					List<RecentWorkspaceEntry> recentWorkspacesFiltered = new ArrayList<>();
					for (RecentWorkspaceEntry recentWorkspaceEntry : recentWorkspaces.getList())
						if (recentWorkspaceEntry.getPath().isFile())
							recentWorkspacesFiltered.add(recentWorkspaceEntry);
					recentWorkspaces = new RecentWorkspaces(recentWorkspacesFiltered);
				}
			} catch (Exception e) {
				recentWorkspaces = null;
				LOG.warn("Failed to load recent workspaces", e);
			}
		}

		if (recentWorkspaces != null && !recentWorkspaces.getList().isEmpty()) {
			defaultListModel.removeAllElements();
			recentWorkspaces.getList().forEach(defaultListModel::addElement);
			recentPanes.show(recentPanel, "recents");
		} else if (recentWorkspaces == null) {
			recentPanes.show(recentPanel, "norecentsloaded");
		} else {
			recentPanes.show(recentPanel, "norecents");
		}

		recentPanel.revalidate();
	}

	private JButton mainWorkspaceButton(String text, ImageIcon icon, ActionListener event) {
		JButton newWorkspace = new JButton(text);
		ComponentUtils.deriveFont(newWorkspace, 16); // Increased font size
		newWorkspace.setBackground(Theme.current().getBackgroundColor());
		newWorkspace.setPreferredSize(new Dimension(300, 60)); // Increased size
		newWorkspace.setIcon(icon);
		newWorkspace.addActionListener(event);
		newWorkspace.setVerticalTextPosition(SwingConstants.CENTER);
		newWorkspace.setHorizontalTextPosition(SwingConstants.RIGHT);
		newWorkspace.setHorizontalAlignment(SwingConstants.LEFT);
		newWorkspace.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return newWorkspace;
	}

	/**
	 * Adds a new "quick start" button to the main panel of the workspace selector.
	 *
	 * @param text  The text displayed by the button being added.
	 * @param icon  The icon to be shown by the button being added.
	 * @param event The action performed when the button is clicked.
	 */
	public void addWorkspaceButton(String text, ImageIcon icon, ActionListener event) {
		JButton workspaceButton = new JButton(text);
		ComponentUtils.deriveFont(workspaceButton, 14); // Increased font size
		workspaceButton.setBackground(Theme.current().getBackgroundColor());
		workspaceButton.setPreferredSize(new Dimension(300, 35)); // Increased size
		workspaceButton.setIcon(
				ImageUtils.drawOver(new EmptyIcon.ImageIcon(45, 16), icon, 45 / 2 - 16 / 2 + 2, 0, 16, 16));
		workspaceButton.addActionListener(event);
		workspaceButton.setVerticalTextPosition(SwingConstants.CENTER);
		workspaceButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		workspaceButton.setHorizontalAlignment(SwingConstants.LEFT);
		workspaceButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		workspaceButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		subactions.add(workspaceButton);
	}

	/*
	 * private void initWebsitePanel() {
	 * // Removed implementation
	 * }
	 */

	@Nonnull
	public RecentWorkspaces getRecentWorkspaces() {
		if (recentWorkspaces == null)
			this.recentWorkspaces = new RecentWorkspaces();

		return recentWorkspaces;
	}

	@Nullable
	public MCreatorApplication getApplication() {
		return application;
	}

	@Override
	public NotificationsRenderer getNotificationsRenderer() {
		return notificationsRenderer;
	}

	private void runAutoPurge() {
		if (PreferencesManager.PREFERENCES.ui.autoPurgeProjects.get()) {
			int months = PreferencesManager.PREFERENCES.ui.autoPurgeProjectsTime.get();
			long threshold = System.currentTimeMillis() - (long) months * 30 * 24 * 60 * 60 * 1000L;
			boolean changed = false;

			List<RecentWorkspaceEntry> currentList = new ArrayList<>(recentWorkspaces.getList());
			for (RecentWorkspaceEntry entry : currentList) {
				if (entry.getPath().exists()) {
					long lastModified = entry.getPath().lastModified();
					if (lastModified < threshold) {
						if (FileIO.moveToTrash(entry.getPath().getParentFile())) {
							recentWorkspaces.getList().remove(entry);
							changed = true;
							LOG.info("Auto-purged project: " + entry.getName());
						}
					}
				}
			}

			if (changed) {
				saveRecentWorkspaces();
				reloadRecents();
			}
		}
	}

}
