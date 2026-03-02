/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2022, Pylo, opensource contributors
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

package net.mcreator.ui.blockly;

import com.formdev.flatlaf.FlatClientProperties;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.blockly.data.ToolboxBlock;
import net.mcreator.blockly.data.ToolboxCategory;
import net.mcreator.blockly.java.BlocklyVariables;
import net.mcreator.blockly.java.ProcedureTemplateIO;
import net.mcreator.io.ResourcePointer;
import net.mcreator.io.TemplatesLoader;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.JScrollablePopupMenu;
import net.mcreator.ui.component.TransparentToolBar;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.file.FileDialogs;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.ui.modgui.ProcedureGUI;
import net.mcreator.util.ColorUtils;
import net.mcreator.util.StringUtils;
import net.mcreator.workspace.elements.VariableElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class BlocklyEditorToolbar extends TransparentToolBar {

	private static final Logger LOG = LogManager.getLogger(BlocklyEditorToolbar.class);

	private JScrollablePopupMenu results = new JScrollablePopupMenu();
	private final JButton templateLib;

	private final BlocklyPanel blocklyPanel;

	private final JTextField search;
	private final javax.swing.Timer searchTimer;

	public BlocklyEditorToolbar(MCreator mcreator, BlocklyEditorType blocklyEditorType, BlocklyPanel blocklyPanel) {
		this(mcreator, blocklyEditorType, blocklyPanel, null, true);
	}

	public BlocklyEditorToolbar(MCreator mcreator, BlocklyEditorType blocklyEditorType, BlocklyPanel blocklyPanel,
			boolean hasSearchBar) {
		this(mcreator, blocklyEditorType, blocklyPanel, null, hasSearchBar);
	}

	public BlocklyEditorToolbar(MCreator mcreator, BlocklyEditorType blocklyEditorType, BlocklyPanel blocklyPanel,
			ProcedureGUI procedureGUI, JComponent... extraComponents) {
		this(mcreator, blocklyEditorType, blocklyPanel, procedureGUI, true, extraComponents);
	}

	/**
	 * <p>
	 * A {@link BlocklyEditorToolbar} is the top panel added on every Java
	 * {@link BlocklyPanel}.
	 * It contains buttons like templates, an export and an import template buttons.
	 * </p>
	 *
	 * @param mcreator
	 *                          <p>
	 *                          The {@link MCreator} instance used
	 *                          </p>
	 * @param blocklyEditorType
	 *                          <p>
	 *                          Type of the Blockly editor this toolbar will be used
	 *                          on.
	 *                          </p>
	 * @param blocklyPanel
	 *                          <p>
	 *                          The {@link BlocklyPanel} to use for some features
	 *                          </p>
	 * @param procedureGUI
	 *                          <p>
	 *                          When a {@link ProcedureGUI} is passed, features
	 *                          specific to
	 *                          {@link net.mcreator.element.types.Procedure} such as
	 *                          variables are enabled.
	 *                          </p>
	 * @param hasSearchBar
	 *                          <p>
	 *                          If this toolbar will have a search bar.
	 *                          </p>
	 * @param extraComponents
	 *                          <p>
	 *                          List of additional {@link JComponent} to show inside
	 *                          the toolbar.
	 *                          </p>
	 */
	public BlocklyEditorToolbar(MCreator mcreator, BlocklyEditorType blocklyEditorType, BlocklyPanel blocklyPanel,
			ProcedureGUI procedureGUI, boolean hasSearchBar, JComponent... extraComponents) {
		this.blocklyPanel = blocklyPanel;

		setBorder(null);

		this.searchTimer = new javax.swing.Timer(150, e -> updateSearch(blocklyEditorType));
		this.searchTimer.setRepeats(false);

		List<ResourcePointer> templates = TemplatesLoader.loadTemplates(blocklyEditorType.extension(),
				blocklyEditorType.extension());

		BlocklyTemplateDropdown templateDropdown = new BlocklyTemplateDropdown(blocklyPanel, templates, procedureGUI);

		templateLib = L10N.button("blockly.templates." + blocklyEditorType.registryName());
		templateLib.setPreferredSize(new Dimension(155, 16));
		templateLib.setIcon(UIRES.get("18px.templatelib"));
		templatesButtonStyle(templateLib);

		if (!templates.isEmpty())
			add(templateLib);

		templateLib.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				templateDropdown.show(e.getComponent(), e.getComponent().getWidth(), 0);
			}
		});

		search = new JTextField() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (getText().isEmpty()) {
					g.setFont(g.getFont().deriveFont(11f));
					g.setColor(new Color(120, 120, 120));
					g.drawString(L10N.t("blockly.search_" + blocklyEditorType.registryName()), 8, 18);
				}
			}
		};
		search.setBackground(ColorUtils.applyAlpha(search.getBackground(), 100));
		search.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				blocklyPanel.setBrowserFocus(false);
				search.grabFocus();
				search.requestFocus();
				search.requestFocusInWindow();
			}
		});

		search.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				blocklyPanel.setBrowserFocus(false);
			}

			@Override
			public void focusLost(FocusEvent e) {
				// Only take focus back if it was stolen by the CefBrowser (native component)
				Component opposite = e.getOppositeComponent();
				boolean isBrowser = opposite != null && opposite.getClass().getName().contains("CefBrowser");

				if (isBrowser && !search.getText().isEmpty()) {
					SwingUtilities.invokeLater(() -> {
						if (search.isShowing() && !search.isFocusOwner()) {
							blocklyPanel.setBrowserFocus(false);
							search.grabFocus();
							search.requestFocusInWindow();
						}
					});
				} else if (!isBrowser) {
					blocklyPanel.setBrowserFocus(true);
				}
			}
		});

		search.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					search.setText("");
					results.setVisible(false);
					blocklyPanel.requestFocusInWindow();
					blocklyPanel.setBrowserFocus(true);
				}
			}
		});
			search.setPreferredSize(new Dimension(340, 22));

			search.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					searchTimer.restart();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					searchTimer.restart();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					searchTimer.restart();
				}
			});

			JComponent searchWrapper = PanelUtils.join(FlowLayout.LEFT, 0, 0, search);
			searchWrapper.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
			searchWrapper.setMaximumSize(
					new Dimension(search.getPreferredSize().width + 1, search.getPreferredSize().height));
			add(searchWrapper);
		}

	for(

	var component:extraComponents)
	{
		add(component);
	}

	add(Box.createHorizontalGlue());

		JButton export = L10N.button("blockly.templates." + blocklyEditorType.registryName() + ".export");
		export.setIcon(UIRES.get("18px.export"));
		add(export);
		export.addActionListener(event -> {
			File exp = FileDialogs.getSaveDialog(mcreator, new String[] { "." + blocklyEditorType.extension() });
			if (exp != null) {
				new Thread(() -> {
					try {
						ProcedureTemplateIO.exportBlocklySetup(blocklyPanel.getXML(), exp, blocklyEditorType);
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
						JOptionPane.showMessageDialog(mcreator,
								L10N.t("blockly.templates." + blocklyEditorType.registryName()
										+ ".export_failed.message"),
								L10N.t("blockly.templates." + blocklyEditorType.registryName()
										+ ".export_failed.title"),
								JOptionPane.WARNING_MESSAGE);
					}
				}, "Blockly-Blocks-Exporter").start();
			}
		});
		styleButton(export);
		export.setForeground(Theme.current().getAltForegroundColor());

		JButton import_ = L10N.button("blockly.templates." + blocklyEditorType.registryName() + ".import");
		import_.setIcon(UIRES.get("18px.import"));
		add(import_);
		import_.addActionListener(event -> {
			File imp = FileDialogs.getOpenDialog(mcreator, new String[] { blocklyEditorType.extension() });
			if (imp != null) {
				//
				// Run import in a separate thread to avoid blocking the UI and to avoid
				// deadlocks on macOS
				new Thread(() -> {
					try {
						String procedureXml = ProcedureTemplateIO.importBlocklyXML(imp);
						if (procedureGUI != null) {
							Set<VariableElement> localVariables = BlocklyVariables.tryToExtractVariables(procedureXml);
							List<VariableElement> existingLocalVariables = blocklyPanel.getLocalVariablesList();

							for (VariableElement localVariable : localVariables) {
								if (existingLocalVariables.contains(localVariable))
									continue; // skip if variable with this name already exists

								blocklyPanel.addLocalVariable(localVariable.getName(),
										localVariable.getType().getBlocklyVariableType());
								procedureGUI.localVars.addElement(localVariable);
							}
						}
						blocklyPanel.addBlocksFromXML(procedureXml);
					} catch (Exception e) {
						LOG.error("Failed to import Blockly template", e);
						SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mcreator,
								L10N.t("blockly.templates." + blocklyEditorType.registryName()
										+ ".import_failed.message"),
								L10N.t("blockly.templates." + blocklyEditorType.registryName()
										+ ".import_failed.title"),
								JOptionPane.WARNING_MESSAGE));
					}
				}, "Blockly-Template-Import").start();
			}
		});
		styleButton(import_);
		import_.setForeground(Theme.current().getAltForegroundColor());
	}

	private void updateSearch(BlocklyEditorType blocklyEditorType) {
		String text = search.getText().trim();
		if (text.isEmpty()) {
			results.setVisible(false);
			return;
		}

		new Thread(() -> {
			String cleanedText = text.replaceAll("[^ \\p{L}\\p{N}/._-]+", "");
			if (cleanedText.isEmpty()) {
				SwingUtilities.invokeLater(() -> results.setVisible(false));
				return;
			}

			String queryLower = cleanedText.toLowerCase(java.util.Locale.ENGLISH);
			String[] keyWords = queryLower.split("\\s+");

			Map<String, ToolboxBlock> definedBlocks = BlocklyLoader.INSTANCE.getBlockLoader(blocklyEditorType)
					.getDefinedBlocks();

			Set<ToolboxBlock> filtered = new LinkedHashSet<>();

			for (ToolboxBlock block : definedBlocks.values()) {
				String blockNameLower = block.getName().toLowerCase(java.util.Locale.ENGLISH);

				// Priority 1: Direct containment of the full cleaned query
				if (blockNameLower.contains(queryLower)) {
					filtered.add(block);
				} else {
					boolean allMatch = true;
					for (String keyWord : keyWords) {
						boolean wordMatches = blockNameLower.contains(keyWord);
						if (!wordMatches && block.getToolboxCategory() != null) {
							wordMatches = block.getToolboxCategory().getName().toLowerCase(java.util.Locale.ENGLISH)
									.contains(keyWord);
						}
						if (!wordMatches) {
							allMatch = false;
							break;
						}
					}

					if (allMatch) {
						filtered.add(block);
					}
				}

				if (filtered.size() >= 25) // Limit results for performance
					break;
			}

			final List<ToolboxBlock> finalFiltered = new ArrayList<>(filtered);
			SwingUtilities.invokeLater(() -> {
				if (!search.getText().trim().equals(text))
					return; // Check if search text changed while filtering

				if (!finalFiltered.isEmpty()) {
					boolean wasVisible = results.isVisible();
					results.removeAll();
					results.setBackground(Theme.current().getBackgroundColor());
					results.setFocusable(false);
					results.setRequestFocusEnabled(false);
					results.setBorder(BorderFactory.createEmptyBorder());
					results.putClientProperty(FlatClientProperties.POPUP_BORDER_CORNER_RADIUS, 0);
					for (ToolboxBlock block : finalFiltered) {
						JMenuItem item = new JMenuItem(getHTMLForBlock(block));
						item.setFocusable(false);
						item.addActionListener(ev -> {
							String xml = block.getToolboxXML();
							if (!xml.startsWith("<xml")) {
								xml = "<xml>" + xml + "</xml>";
							}
							blocklyPanel.addBlocksFromXML(xml);
							search.setText("");
							results.setVisible(false);
						});
						results.add(item);
					}

					if (search.isFocusOwner()) {
						if (!wasVisible) {
							results.show(search, 0, search.getHeight() + 2);
						} else {
							results.revalidate();
							results.repaint();
						}
						SwingUtilities.invokeLater(search::requestFocusInWindow);
					}
				} else {
					results.setVisible(false);
				}
			});
		}, "Blockly-Search-Thread").start();
	}

	public void setTemplateLibButtonWidth(int w) {
		templateLib.setPreferredSize(new Dimension(w, 16));
	}

	private String getHTMLForBlock(ToolboxBlock block) {
		StringBuilder builder = new StringBuilder("<html>");

		List<ToolboxCategory> categories = new ArrayList<>();
		traverseCategories(categories, block.getToolboxCategory());

		if (categories.isEmpty()) {
			String category_raw = block.getToolboxCategoryRaw();
			if (category_raw != null && !category_raw.isBlank()) {
				categories.add(ToolboxCategory.tryGetBuiltin(category_raw));
			}
		}

		for (int i = categories.size() - 1; i >= 0; i--) {
			ToolboxCategory category = categories.get(i);
			builder.append("<span style='background: #")
					.append(Integer.toHexString(category.getColor().getRGB()).substring(2)).append(";'>&nbsp;")
					.append(category.getName()).append("&nbsp;</span>");
			if (i != 0)
				builder.append("<span style='background: #444444;'>&nbsp;&#x25B8;&nbsp;</span>");
		}

		String name = block.getName();
		name = StringUtils.abbreviateString(name, 130); // make sure we don't display too long texts
		name = name.replaceAll("[0-9%]+(?:\\\\.\\\\.\\\\.)?$",
				""); // make sure to strip away potential reminders of %N parameters

		builder.append("&nbsp;&nbsp;");
		builder.append(name.replaceAll("%\\d+?",
				"&nbsp;<span style='background: #444444'>&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;"));

		return builder.toString();
	}

	private void traverseCategories(List<ToolboxCategory> categories, ToolboxCategory category) {
		if (category != null) {
			categories.add(category);
			if (category.getParent() != null)
				traverseCategories(categories, category.getParent());
		}
	}

	public static void styleButton(AbstractButton button) {
		button.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(0, 0, 0, 0)),
						BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1),
								BorderFactory.createMatteBorder(1, 3, 1, 5, new Color(0, 0, 0, 0)))));
		ComponentUtils.deriveFont(button, 11);
	}

	private static void templatesButtonStyle(AbstractButton button) {
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("Component.borderColor")),
				BorderFactory.createEmptyBorder(1, 0, 1, 0)));
		ComponentUtils.deriveFont(button, 11);
	}

	public JTextField getSearchField() {
		return search;
	}

}
