package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;

import edu.cwru.eecs341project.GlobalState;

public class MainWindow extends MicrocenterWindow {
	private Panel actionsPanel;
	
	public MainWindow(GUIScreen guiScreen) {
		super(guiScreen, "Microcenter Store Application", false, false);
		
		updateActionList();
	}
	
	private void updateActionList() {
		GlobalState.UserRole role = GlobalState.getUserRole();
		removeComponent(actionsPanel);
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Stores"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Products"));
		if(role != GlobalState.UserRole.ANONYMOUS)
			actionListBox.addAction(new ActionListBoxItem(guiScreen, "Customers"));
		if(role == GlobalState.UserRole.EMPLOYEE || role == GlobalState.UserRole.DBA)
			actionListBox.addAction(new ActionListBoxItem(guiScreen, "User Management"));
		if(role == GlobalState.UserRole.DBA)
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
        	if(label.equals("User Management"))
        	{
        		guiScreen.showWindow(new UserManagementWindow(guiScreen, "User Management", false), GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Database")) {
        		guiScreen.showWindow(new DatabaseWindow(guiScreen, "Database"), GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Products")) {
        		guiScreen.showWindow(new ProductsWindow(guiScreen), GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Stores")) {
        		guiScreen.showWindow(new StoresWindow(guiScreen), GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Customers")) {
        		guiScreen.showWindow(new CustomersWindow(guiScreen), GUIScreen.Position.FULL_SCREEN);
        	} else {
        		MessageBox.showMessageBox(owner, "Action", "Selected " + label);
        	}
        }
	}
	
	@Override
	public void refresh() {
		updateActionList();
		super.refresh();
	}
	
	@Override
	public void close() {
		GlobalState.closeDBConnection();
		super.close();
	}
}
