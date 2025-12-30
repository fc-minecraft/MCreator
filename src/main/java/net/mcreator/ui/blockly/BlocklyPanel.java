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

package net.mcreator.ui.blockly;

import com.google.gson.Gson;
import net.mcreator.blockly.data.ExternalTrigger;
import net.mcreator.io.FileIO;
import net.mcreator.io.OS;
import net.mcreator.minecraft.DataListLoader;
import net.mcreator.plugin.MCREvent;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.plugin.events.ui.BlocklyPanelRegisterJSObjects;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.ThreadUtil;
import net.mcreator.ui.init.BlocklyJavaScriptsLoader;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.util.TestUtil;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.VariableElement;
import net.mcreator.workspace.elements.VariableType;
import net.mcreator.workspace.elements.VariableTypeLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.Closeable;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class BlocklyPanel extends JPanel implements Closeable {

	private static final Logger LOG = LogManager.getLogger("Blockly");
	public static final Map<CefBrowser, Workspace> browserWorkspaceMap = new ConcurrentHashMap<>();

	private CefClient client;
	private CefBrowser browser;

	private final BlocklyJavascriptBridge bridge;
	private final BlockingQueue<Runnable> runAfterLoaded = new LinkedBlockingQueue<>();
	private boolean loaded = false;

	private final MCreator mcreator;
	private final BlocklyEditorType type;

	private final List<ChangeListener> changeListeners = new CopyOnWriteArrayList<>();

	private String lastKnownXML = "";
	private List<VariableElement> localVariables = new ArrayList<>();

	private final JLabel loadingLabel;

	public BlocklyPanel(MCreator mcreator, @Nonnull BlocklyEditorType type) {
		super(new BorderLayout());
		this.mcreator = mcreator;
		this.type = type;

		bridge = new BlocklyJavascriptBridge(mcreator, () -> ThreadUtil.runOnSwingThread(
				() -> changeListeners.forEach(listener -> listener.stateChanged(new ChangeEvent(BlocklyPanel.this)))));

		loadingLabel = new JLabel("Initializing JCEF...", SwingConstants.CENTER);
		loadingLabel.setFont(loadingLabel.getFont().deriveFont(16f));
		add(loadingLabel, BorderLayout.CENTER);

		Thread initThread = new Thread(() -> {
			JCEFHelper.initialize(status -> SwingUtilities.invokeLater(() -> loadingLabel.setText(status)));

			SwingUtilities.invokeLater(() -> {
				client = JCEFHelper.createClient();
				if (client == null) {
					LOG.error("Failed to create JCEF client");
					loadingLabel.setText("Failed to initialize JCEF.");
					return;
				}

				// Add Console Logging
				client.addDisplayHandler(new CefDisplayHandlerAdapter() {
					@Override
					public boolean onConsoleMessage(CefBrowser browser, org.cef.CefSettings.LogSeverity level, String message, String source, int line) {
						String logMsg = "[JS] " + message + " (" + source + ":" + line + ")";
						switch (level) {
							case LOGSEVERITY_ERROR: LOG.error(logMsg); break;
							case LOGSEVERITY_WARNING: LOG.warn(logMsg); break;
							case LOGSEVERITY_INFO: LOG.info(logMsg); break;
							default: LOG.debug(logMsg); break;
						}
						return false; // Allow default handling? or true to suppress
					}
				});

				// Message Router
				CefMessageRouter msgRouter = CefMessageRouter.create();
				msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
					@Override
					public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request, boolean persistent,
							CefQueryCallback callback) {
						if (request.startsWith("t:")) {
							String key = request.substring(2);
							callback.success(bridge.t(key));
							return true;
						} else if (request.equals("triggerEvent")) {
							bridge.triggerEvent();
							callback.success("");
							return true;
						} else if (request.startsWith("startBlockForEditor:")) {
							String editor = request.substring("startBlockForEditor:".length());
							String result = bridge.startBlockForEditor(editor);
							callback.success(result != null ? result : "");
							return true;
						} else if (request.startsWith("updateXML:")) {
							lastKnownXML = request.substring("updateXML:".length());
							callback.success("");
							return true;
						} else if (request.startsWith("updateLocalVariables:")) {
							updateLocalVariables(request.substring("updateLocalVariables:".length()));
							callback.success("");
							return true;
						} else if (request.startsWith("openColorSelector:")) {
							String[] parts = request.split(":", 3);
							if (parts.length == 3) {
								String id = parts[1];
								String color = parts[2];
								bridge.openColorSelector(color, (result) -> {
									String res = (String) result;
									browser.executeJavaScript("if(window.javabridge.callbacks['" + id + "']) window.javabridge.callbacks['" + id + "'].callback('" + (res!=null?res:"") + "'); delete window.javabridge.callbacks['" + id + "'];", browser.getURL(), 0);
								});
								callback.success("");
								return true;
							}
						} else if (request.startsWith("openMCItemSelector:")) {
							String[] parts = request.split(":", 3);
							if (parts.length == 3) {
								String id = parts[1];
								String type = parts[2];
								bridge.openMCItemSelector(type, (result) -> {
									String res = (String) result;
									browser.executeJavaScript("if(window.javabridge.callbacks['" + id + "']) window.javabridge.callbacks['" + id + "'].callback('" + (res!=null?res:"") + "'); delete window.javabridge.callbacks['" + id + "'];", browser.getURL(), 0);
								});
								callback.success("");
								return true;
							}
						} else if (request.startsWith("openEntrySelector:")) {
							// Format: openEntrySelector:ID:type:typeFilter:customEntryProviders
							String[] parts = request.split(":", 5);
							if (parts.length >= 3) {
								String id = parts[1];
								String type = parts[2];
								String typeFilter = parts.length > 3 ? parts[3] : null;
								String customEntryProviders = parts.length > 4 ? parts[4] : null;

								if ("null".equals(typeFilter)) typeFilter = null;
								if ("null".equals(customEntryProviders)) customEntryProviders = null;

								bridge.openEntrySelector(type, typeFilter, customEntryProviders, (result) -> {
									String[] res = (String[]) result;
									// result is {value, readableName}
									String val = res[0].replace("'", "\\'");
									String name = res[1].replace("'", "\\'");
									browser.executeJavaScript("if(window.javabridge.callbacks['" + id + "']) window.javabridge.callbacks['" + id + "'].callback('" + val + "', '" + name + "'); delete window.javabridge.callbacks['" + id + "'];", browser.getURL(), 0);
								});
								callback.success("");
								return true;
							}
						} else if (request.startsWith("openAIConditionEditor:")) {
							String[] parts = request.split(":", 3);
							if (parts.length == 3) {
								String id = parts[1];
								String data = parts[2];
								bridge.openAIConditionEditor(data, (result) -> {
									String res = (String) result;
									browser.executeJavaScript("if(window.javabridge.callbacks['" + id + "']) window.javabridge.callbacks['" + id + "'].callback('" + (res!=null?res:"") + "'); delete window.javabridge.callbacks['" + id + "'];", browser.getURL(), 0);
								});
								callback.success("");
								return true;
							}
						}
						return false;
					}
				}, true);
				client.addMessageRouter(msgRouter);

				browser = client.createBrowser("client://mcreator/blockly/blockly.html", false, false);
				browserWorkspaceMap.put(browser, mcreator.getWorkspace());

				client.addLoadHandler(new CefLoadHandlerAdapter() {
					@Override
					public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
						if (frame.isMain()) {
							injectSetupScripts();
							loaded = true;
							runAfterLoaded.forEach(ThreadUtil::runOnSwingThread);
							runAfterLoaded.clear();
						}
					}
				});

				removeAll();
				add(browser.getUIComponent(), BorderLayout.CENTER);
				revalidate();
				repaint();
			});
		});
		initThread.start();
	}

	private void updateLocalVariables(String query) {
		List<VariableElement> retval = new ArrayList<>();
		if (query == null || query.isEmpty()) {
			localVariables = retval;
			return;
		}

		String[] vars = query.split(":");
		for (String varNameType : vars) {
			String[] vardata = varNameType.split(";");
			if (vardata.length == 2) {
				VariableType variableType = VariableTypeLoader.INSTANCE.fromName(vardata[1]);
				if (variableType != null) {
					VariableElement element = new VariableElement(vardata[0]);
					element.setType(variableType);
					retval.add(element);
				}
			}
		}
		localVariables = retval;
	}

	private void injectSetupScripts() {
		StringBuilder initScript = new StringBuilder();

		initScript.append("window.javabridge = {};\n");
		initScript.append("window.javabridge.callbacks = {};\n");
		initScript.append("window.javabridge.triggerEvent = function() { window.cefQuery({request: 'triggerEvent', persistent: false, onSuccess: function(r){}, onFailure: function(e,m){}}); };\n");

		initScript.append("window.javabridge.t = function(key) { return key; };\n");
		initScript.append("window.javabridge.getMCItemURI = function(name) { return 'client://mcreator/icon/' + name + '.png'; };\n");

		String startBlock = bridge.startBlockForEditor(type.registryName());
		initScript.append("window.javabridge.startBlockForEditor = function(editor) { return '" + (startBlock!=null?startBlock:"") + "'; };\n");

		// Helper for callbacks
		initScript.append("""
			window.javabridge.registerCallback = function(callback) {
				var id = 'cb_' + Math.floor(Math.random() * 1000000);
				window.javabridge.callbacks[id] = callback;
				return id;
			};
		""");

		initScript.append("window.javabridge.openColorSelector = function(color, callback) { var id = window.javabridge.registerCallback(callback); window.cefQuery({request: 'openColorSelector:' + id + ':' + color, persistent: false, onSuccess: function(r){}, onFailure: function(e,m){}}); };\n");
		initScript.append("window.javabridge.openMCItemSelector = function(type, callback) { var id = window.javabridge.registerCallback(callback); window.cefQuery({request: 'openMCItemSelector:' + id + ':' + type, persistent: false, onSuccess: function(r){}, onFailure: function(e,m){}}); };\n");
		initScript.append("window.javabridge.openAIConditionEditor = function(data, callback) { var id = window.javabridge.registerCallback(callback); window.cefQuery({request: 'openAIConditionEditor:' + id + ':' + data, persistent: false, onSuccess: function(r){}, onFailure: function(e,m){}}); };\n");
		initScript.append("window.javabridge.openEntrySelector = function(type, typeFilter, customEntryProviders, callback) { var id = window.javabridge.registerCallback(callback); window.cefQuery({request: 'openEntrySelector:' + id + ':' + type + ':' + (typeFilter?typeFilter:'null') + ':' + (customEntryProviders?customEntryProviders:'null'), persistent: false, onSuccess: function(r){}, onFailure: function(e,m){}}); };\n");

		// Preload Lists
		initScript.append("window.MCR_LISTS = {};\n");
		Gson gson = new Gson();

		Set<String> datalists = new HashSet<>(DataListLoader.getCache().keySet());

		String[] explicitTypes = {"procedure", "entity", "spawnableEntity", "gui", "achievement", "effect", "potion",
                                  "gamerulesboolean", "gamerulesnumber", "fluid", "sound", "particle", "direction",
                                  "schematic", "enhancement", "biome", "dimension_custom", "villagerprofessions"};
        datalists.addAll(Arrays.asList(explicitTypes));

        for (VariableType vt : VariableTypeLoader.INSTANCE.getAllVariableTypes()) {
             datalists.add("procedure_retval_" + vt.getName());
        }

		for (String t : datalists) {
			try {
				String[] list = BlocklyJavascriptBridge.getListOfForWorkspace(mcreator.getWorkspace(), t);
				if (list != null) {
					String json = gson.toJson(list);
					initScript.append("window.MCR_LISTS['" + t + "'] = " + json + ";\n");
				}
			} catch (Exception e) {
				LOG.warn("Failed to preload list: " + t, e);
			}
		}

		browser.executeJavaScript(initScript.toString(), browser.getURL(), 0);

		// Load CSS
		String css = FileIO.readResourceToString("/blockly/css/mcreator_blockly.css");
		if (PluginLoader.INSTANCE.getResourceAsStream(
				"themes/" + Theme.current().getID() + "/styles/blockly.css") != null) {
			css += FileIO.readResourceToString(PluginLoader.INSTANCE,
					"/themes/" + Theme.current().getID() + "/styles/blockly.css");
		} else {
			css += FileIO.readResourceToString(PluginLoader.INSTANCE,
					"/themes/default_dark/styles/blockly.css");
		}

		if (PreferencesManager.PREFERENCES.blockly.legacyFont.get()) {
			css = css.replace("font-family: sans-serif;", "");
		}

		// Inject CSS
		String cssScript = "var style = document.createElement('style'); style.innerHTML = `" + css.replace("`", "\\`") + "`; document.head.appendChild(style);";
		browser.executeJavaScript(cssScript, browser.getURL(), 0);

		// Initialize Window Members
		String jsMembers = "window.editorType = '" + type.registryName() + "';";
		browser.executeJavaScript(jsMembers, browser.getURL(), 0);

		// Preferences
		String prefScript = "var MCR_BLOCKLY_PREF = { "
						+ "'comments' : " + PreferencesManager.PREFERENCES.blockly.enableComments.get() + ","
						+ "'renderer' : '" + PreferencesManager.PREFERENCES.blockly.blockRenderer.get().toLowerCase(Locale.ENGLISH) + "',"
						+ "'collapse' : " + PreferencesManager.PREFERENCES.blockly.enableCollapse.get() + ","
						+ "'trashcan' : " + PreferencesManager.PREFERENCES.blockly.enableTrashcan.get() + ","
						+ "'maxScale' : " + PreferencesManager.PREFERENCES.blockly.maxScale.get() / 100.0 + ","
						+ "'minScale' : " + PreferencesManager.PREFERENCES.blockly.minScale.get() / 100.0 + ","
						+ "'scaleSpeed' : " + PreferencesManager.PREFERENCES.blockly.scaleSpeed.get() / 100.0 + ","
						+ "'saturation' :" + PreferencesManager.PREFERENCES.blockly.colorSaturation.get() / 100.0 + ","
						+ "'value' :" + PreferencesManager.PREFERENCES.blockly.colorValue.get() / 100.0
						+ " };";
		browser.executeJavaScript(prefScript, browser.getURL(), 0);

		// Load Scripts
		loadScript("/jsdist/blockly_compressed.js");
		loadScript("/jsdist/msg/" + L10N.getBlocklyLangName() + ".js");
		loadScript("/jsdist/blocks_compressed.js");
		loadScript("/blockly/js/mcreator_blockly.js");

		for (String script : BlocklyJavaScriptsLoader.INSTANCE.getScripts())
			browser.executeJavaScript(script, browser.getURL(), 0);

		browser.executeJavaScript(VariableTypeLoader.INSTANCE.getVariableBlocklyJS(), browser.getURL(), 0);
	}

	private void loadScript(String path) {
		String content = FileIO.readResourceToString(path);
		if (content != null) {
			browser.executeJavaScript(content, browser.getURL(), 0);
		}
	}

	public void addTaskToRunAfterLoaded(Runnable runnable) {
		if (!loaded)
			runAfterLoaded.add(runnable);
		else
			runnable.run();
	}

	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}

	public synchronized String getXML() {
		return lastKnownXML;
	}

	public void setXML(String xml) {
		String cleanXML = xml.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
		String script = """
				workspace.clear();
				Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom('%s'), workspace);
				workspace.clearUndo();
				""".formatted(cleanXML);
		if (browser != null) browser.executeJavaScript(script, browser.getURL(), 0);

		ThreadUtil.runOnSwingThread(
				() -> changeListeners.forEach(listener -> listener.stateChanged(new ChangeEvent(xml))));
	}

	public void addBlocksFromXML(String xml) {
		if (browser == null) return;
		String cleanXML = xml.replace("xmlns=\"http://www.w3.org/1999/xhtml\"", "")
				.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");

		int index = cleanXML.indexOf("</block><block");
		if (index == -1) {
			browser.executeJavaScript(
					"Blockly.Xml.appendDomToWorkspace(Blockly.Xml.textToDom('" + cleanXML + "'), workspace)", browser.getURL(), 0);
		} else {
			index += 8;
			browser.executeJavaScript(
					"Blockly.Xml.appendDomToWorkspace(Blockly.Xml.textToDom('" + cleanXML.substring(0, index)
							+ "</xml>'), workspace)", browser.getURL(), 0);
			browser.executeJavaScript(
					"Blockly.Xml.appendDomToWorkspace(Blockly.Xml.textToDom('<xml>" + cleanXML.substring(index)
							+ "'), workspace)", browser.getURL(), 0);
		}
	}

	public void addGlobalVariable(String name, String type) {
		if (browser != null) browser.executeJavaScript("global_variables.push({name: '" + name + "', type: '" + type + "'})", browser.getURL(), 0);
	}

	public void addLocalVariable(String name, String type) {
		if (browser != null) browser.executeJavaScript("workspace.createVariable('" + name + "', '" + type + "', '" + name + "')", browser.getURL(), 0);
	}

	public void removeLocalVariable(String name) {
		if (browser != null) browser.executeJavaScript("workspace.deleteVariableById('" + name + "')", browser.getURL(), 0);
	}

	public List<VariableElement> getLocalVariablesList() {
		return localVariables;
	}

	public void addExternalTriggerForProcedureEditor(ExternalTrigger external_trigger) {
		if (type != BlocklyEditorType.PROCEDURE)
			throw new RuntimeException("This method can only be called from procedure editor");
		bridge.addExternalTrigger(external_trigger);
	}

	@Override public void close() {
		if (client != null) {
			if (browser != null) {
				browserWorkspaceMap.remove(browser);
				browser.close(true);
				browser = null;
			}
		}
	}

	public MCreator getMCreator() {
		return mcreator;
	}

	public BlocklyEditorType getType() {
		return type;
	}

	public Object executeJavaScriptSynchronously(String script) {
		if (browser != null) browser.executeJavaScript(script, browser.getURL(), 0);
		return null;
	}
}
