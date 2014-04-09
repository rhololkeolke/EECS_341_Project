package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EditArea;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.terminal.TerminalSize;

import edu.cwru.eecs341project.WindowManager;

public class DatabaseWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;
	
	public DatabaseWindow(GUIScreen guiScreen, String label) {
		super(guiScreen, label, true, false);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem("Raw Query"));
		actionListBox.addAction(new ActionListBoxItem("Show Tables"));
		actionListBox.addAction(new ActionListBoxItem("Show Table Schema"));
		actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
	}
	
	private class RawQueryWindow extends MicrocenterWindow {
		private Panel inputPanel;
		private EditArea queryBox;
		private TextArea queryResult;
		public RawQueryWindow(final GUIScreen guiScreen)
		{
			super(guiScreen, "Raw SQL Query", true, false);
			
			inputPanel = new Panel();
			inputPanel.addComponent(new Label("Enter a query to execute"));
			queryBox = new EditArea(guiScreen.getScreen().getTerminalSize(), "SELECT * FROM customer LIMIT 10");
			inputPanel.addComponent(queryBox);
	
			inputPanel.addComponent(new Button("Execute", new Action() {
				@Override
				public void doAction() {
					MessageBox.showMessageBox(guiScreen, "Query Result", "Executing");
					
					// TODO actually execute query here
					
					inputPanel.removeComponent(queryResult);
					queryResult = new TextArea(guiScreen.getScreen().getTerminalSize(), "Not yet implemented\ntesting new line");
					inputPanel.addComponent(queryResult);
					WindowManager.refreshWindow();
				}
			}));
			addComponent(inputPanel);
		}
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
        	if(label.equals("Raw Query")) {
        		//TextInputDialog.showTextInputBox(guiScreen, "SQL Query", "Enter query to execute", "SELECT * FROM customer LIMIT 10");
        		guiScreen.showWindow(new RawQueryWindow(guiScreen), GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Show Tables")) {
        		
        	} else if(label.equals("Show Table Schmea")) {
        		
        	}
        }
	}
}
