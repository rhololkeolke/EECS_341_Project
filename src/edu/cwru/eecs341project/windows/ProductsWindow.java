package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;

public class ProductsWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;

	public ProductsWindow(GUIScreen guiScreen) {
		super(guiScreen, "Products", true, false);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem("Search"));
		actionListBox.addAction(new ActionListBoxItem("Browse"));
		actionListBox.addAction(new ActionListBoxItem("Product Info"));
		actionListBox.addAction(new ActionListBoxItem("Compare Products"));
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