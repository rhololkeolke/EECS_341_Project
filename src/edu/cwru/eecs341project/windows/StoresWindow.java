package edu.cwru.eecs341project.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;

import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

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
        	if(label.equals("List Stores"))
        	{
        		Connection dbConn = GlobalState.getDBConnection();
        		try {
        			Statement st = dbConn.createStatement();
	        		ResultSet rs = st.executeQuery(
	        					"SELECT id, name " +
	        					"FROM store " +
	        					"WHERE name NOT LIKE 'Website';");
	        		
	        		List<String> options = new ArrayList<String>();
	        		Map<String, Integer> storeIds = new HashMap<String, Integer>();
	        		
	        		while(rs.next())
	        		{
	        			options.add("["+rs.getInt(1)+"]" + rs.getString(2));
	        			storeIds.put("["+rs.getInt(1)+"]"+rs.getString(2), rs.getInt(1));
	        		}
	        		
	        		String selected = (String)ListSelectDialog.showDialog(guiScreen, "Stores", "Select a store", options.toArray());
	        		if(selected == null)
	        			return; // user canceled
	        		StoreInfoWindow window = new StoreInfoWindow(guiScreen, storeIds.get(selected));
	        		WindowManager.pushWindow(window);
	        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
	        		rs.close();
	        		st.close();
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
        		}
        	}
        	else if(label.equals("Search Stores")) {
        		Connection dbConn = GlobalState.getDBConnection();
        		   		
        		try {
		        	String searchString = TextInputDialog.showTextInputBox(guiScreen, "Store Search", "Enter a state abbreviation (e.g. OH)", "");
		        			
		        			
	        		String state = searchString;
	        		PreparedStatement st = dbConn.prepareStatement(
	        						"SELECT id, name " +
	        						"FROM store " +
	        						"WHERE LOWER(state) LIKE LOWER(?)  AND " +
	        						"      name NOT LIKE 'Website';");
	        		st.setString(1, state);
		        	ResultSet rs = st.executeQuery();
	
		        	List<String> options = new ArrayList<String>();
		        	Map<String, Integer> storeIds = new HashMap<String, Integer>();
		        		
		        	while(rs.next())
		        	{
		        		options.add("["+rs.getInt(1)+"]" + rs.getString(2));
		        		storeIds.put("["+rs.getInt(1)+"]"+rs.getString(2), rs.getInt(1));
		        	}
		        		
		        	String selected = (String)ListSelectDialog.showDialog(guiScreen, "Stores", "Select a store", options.toArray());
		        	if(selected == null)
		        		return; // user canceled
		        	StoreInfoWindow window = new StoreInfoWindow(guiScreen, storeIds.get(selected));
		        	WindowManager.pushWindow(window);
		        	guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
		        	rs.close();
		        	st.close();
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
        		}
        		
        	} else if(label.equals("Store Info")) {
        		String strId = TextInputDialog.showTextInputBox(guiScreen, "Store ID", "Enter the store ID", "");
        		if(strId == null || strId.length() == 0)
        			return;
        		int storeId = Integer.parseInt(strId);
        		StoreInfoWindow window = new StoreInfoWindow(guiScreen, storeId);
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	}
        	else {
        		MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        	}
        }
	}
	
	public class StoreInfoWindow extends MicrocenterWindow {
		Panel mainPanel;
		public StoreInfoWindow(final GUIScreen guiScreen, int storeId)
		{
			super(guiScreen, "Store Info", true);
			
			mainPanel = new Panel();
			
			Panel infoPanel = new Panel(Panel.Orientation.HORISONTAL);
			Panel leftPanel = new Panel();
			Panel rightPanel = new Panel();
			
			Connection dbConn = GlobalState.getDBConnection();
			try {
				PreparedStatement st = dbConn.prepareStatement(
						"SELECT id, name, opening_date, street1, street2, city, state, zip " +
						"FROM store " +
						"WHERE id = ?;");
				st.setInt(1, storeId);
				ResultSet rs = st.executeQuery();
			
				if(!rs.next())
				{
					rs.close();
					st.close();
					MessageBox.showMessageBox(guiScreen, "Error", "Could not find store specified");
					close();
					return;
				}
				
				leftPanel.addComponent(new Label("ID: "));
				rightPanel.addComponent(new Label(""+storeId));
				leftPanel.addComponent(new Label("Name: "));
				rightPanel.addComponent(new Label(rs.getString(2)));
				leftPanel.addComponent(new Label("Opening Date: "));
				rightPanel.addComponent(new Label(rs.getDate(3).toString()));
				leftPanel.addComponent(new Label("Street 1: "));
				rightPanel.addComponent(new Label(rs.getString(4)));
				leftPanel.addComponent(new Label("Street 2: "));
				rightPanel.addComponent(new Label(rs.getString(5)));
				leftPanel.addComponent(new Label("City: "));
				rightPanel.addComponent(new Label(rs.getString(6)));
				leftPanel.addComponent(new Label("State: "));
				rightPanel.addComponent(new Label(rs.getString(7)));
				leftPanel.addComponent(new Label("Zip: "));
				rightPanel.addComponent(new Label(rs.getString(8)));
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
			}
			
			infoPanel.addComponent(leftPanel);
			infoPanel.addComponent(rightPanel);
			mainPanel.addComponent(infoPanel);
			
			
			
			mainPanel.addComponent(new Button("Refresh"));
			
			addComponent(mainPanel);
		}
	}
}
