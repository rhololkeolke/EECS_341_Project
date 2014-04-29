package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;

public class StoresWindow extends MicrocenterWindow {
	private Panel actionsPanel;

	public StoresWindow(GUIScreen guiScreen) {
		super(guiScreen, "Stores", true);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem("List Stores"));
		actionListBox.addAction(new ActionListBoxItem("Search Stores"));
		actionListBox.addAction(new ActionListBoxItem("Store Info"));
		actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
	}
	
	private class ActionListBoxItem implements Action {
        private String label;

        public ActionListBoxItem(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }

        public void doAction() {
            MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        }
	}
}
