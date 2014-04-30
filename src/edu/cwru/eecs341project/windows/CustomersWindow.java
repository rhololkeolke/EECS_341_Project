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

public class CustomersWindow extends MicrocenterWindow {
	private Panel actionsPanel;

	public CustomersWindow(GUIScreen guiScreen) {
		super(guiScreen, "Customers", true);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem("Search"));
		actionListBox.addAction(new ActionListBoxItem("List Customers"));
		actionListBox.addAction(new ActionListBoxItem("Customer Info"));
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
        	if(label.equals("List Customers"))
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
        		MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        	}
        }
	}
}
