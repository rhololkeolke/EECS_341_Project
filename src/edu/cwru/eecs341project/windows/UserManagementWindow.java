package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;

public class UserManagementWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;

	public UserManagementWindow(GUIScreen guiScreen, String label, boolean checkout) {
		super(guiScreen, label, true, checkout);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Create User"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "List Users"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Delete User"));
		actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
	}
	
	private class ActionListBoxItem implements Action {
        private GUIScreen owner;
        private String label;

        public ActionListBoxItem(GUIScreen owner, String label) {
            this.label = label;
            this.owner = owner;
        }
        
        @Override
        public String toString() {
            return label;
        }

        public void doAction() {
            MessageBox.showMessageBox(owner, "Action", "Selected " + label);
        }
	}

}
