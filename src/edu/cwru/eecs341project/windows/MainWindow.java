package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.input.Key;

public class MainWindow extends Window {
	public MainWindow(GUIScreen guiScreen) {
		super("Microcenter Store Application");

		Panel actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Login"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Logout"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Register"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "User Management"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Stores"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Products"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Customers"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Checkout"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Database"));
        actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
        
        addComponent(new Button("Exit", new CloseAction()));
        actionsPanel.addShortcut(Key.Kind.Escape, new CloseAction());
	}
	
	private class CloseAction implements Action {
		@Override
		public void doAction() {
			MainWindow.this.close();
		}
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
