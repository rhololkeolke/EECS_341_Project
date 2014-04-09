package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.input.Key;

public class MainWindow extends MicrocenterWindow {
	public MainWindow(final GUIScreen guiScreen) {
		super(guiScreen, "Microcenter Store Application", false, false);
		
		Panel actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "User Management"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Stores"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Products"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Customers"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Database"));
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
