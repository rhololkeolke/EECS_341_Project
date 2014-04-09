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

public class MainWindow extends Window {
	public MainWindow(GUIScreen guiScreen) {
		super("Microcenter Store Application");
		
		Panel mainPanel = new Panel();
		
		Panel menuBar = new Panel(new Border.Bevel(true), Panel.Orientation.HORISONTAL);
		menuBar.addComponent(new Button("Exit (Esc)", new CloseAction()));
		menuBar.addComponent(new Label("              "));
		menuBar.addComponent(new Button("Checkout (Ctrl-C)")); // TODO: Implement hiding and action
		menuBar.addComponent(new Button("Login (Ctrl-l)")); // TODO: Implement hiding and action
		menuBar.addComponent(new Button("Register (Ctrl-r)")); // TODO: Implement hiding and action
		mainPanel.addComponent(menuBar);
		
		mainPanel.addComponent(new Label(""));
		mainPanel.addComponent(new Label(""));
		
		Panel actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "User Management"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Stores"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Products"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Customers"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Database"));
        actionsPanel.addComponent(actionListBox);
        mainPanel.addComponent(actionsPanel);
        
        mainPanel.addShortcut(Key.Kind.Escape, new CloseAction());
        
        addComponent(mainPanel);

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
