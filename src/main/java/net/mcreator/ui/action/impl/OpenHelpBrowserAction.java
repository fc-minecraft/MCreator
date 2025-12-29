package net.mcreator.ui.action.impl;

import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.BasicAction;
import net.mcreator.ui.help.HelpBrowser;
import net.mcreator.ui.init.L10N;

import java.awt.event.ActionEvent;

public class OpenHelpBrowserAction extends BasicAction {

    public OpenHelpBrowserAction(ActionRegistry actionRegistry) {
        super(actionRegistry, L10N.t("action.wiki"), (e) -> {
            HelpBrowser.open();
        });
    }
}
