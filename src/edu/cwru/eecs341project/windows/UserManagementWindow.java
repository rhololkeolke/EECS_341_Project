package edu.cwru.eecs341project.windows;

import java.sql.Connection;
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
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;

import edu.cwru.eecs341project.GlobalState;

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
        	if(label.equals("List Users"))
        	{
        		Connection dbConn = GlobalState.getDBConnection();
        		List<String> listOptions = new ArrayList<String>();
        		Map<String, Integer> nameToLoyaltyMap = new HashMap<String, Integer>();
        		try {
        			Statement st = dbConn.createStatement();
        			ResultSet rs = st.executeQuery("SELECT loyalty_number, last_name, first_name, middle_initial FROM customer ORDER BY last_name, first_name, middle_initial;");
        			while(rs.next())
        			{
        				StringBuilder nameBuilder = new StringBuilder();
        				nameBuilder.append(rs.getString(2));
        				nameBuilder.append(", ");
        				nameBuilder.append(rs.getString(3));
        				nameBuilder.append(" ");
        				String middleInitial = rs.getString(4);
        				if(middleInitial != null)
        				{
        					nameBuilder.append(middleInitial);
        					nameBuilder.append(".");
        				}
        				
        				nameToLoyaltyMap.put(nameBuilder.toString(), rs.getInt(1));
        				listOptions.add("[" + rs.getInt(1) + "]" + nameBuilder.toString());
        			}
        			
        			ListSelectDialog.showDialog(guiScreen, "Customer List", "All customers in database", listOptions.toArray());
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", "Error getting list of customers" + e.getMessage());
        			return;
        		}
        	}
        	else {
        		MessageBox.showMessageBox(owner, "Action", "Selected " + label);
        	}
        }
	}

}
