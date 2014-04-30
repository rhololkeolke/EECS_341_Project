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
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;

import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

public class CustomersWindow extends MicrocenterWindow {
	private Panel actionsPanel;

	public CustomersWindow(GUIScreen guiScreen) {
		super(guiScreen, "Customers", true);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		if(GlobalState.getUserRole() == GlobalState.UserRole.EMPLOYEE || GlobalState.getUserRole() == GlobalState.UserRole.DBA)
		{
			actionListBox.addAction(new ActionListBoxItem("Search"));
			actionListBox.addAction(new ActionListBoxItem("List Customers"));
		}
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
        	if(label.equals("List Customers") || label.equals("Search"))
        	{
        		Connection dbConn = GlobalState.getDBConnection();
        		List<String> listOptions = new ArrayList<String>();
        		Map<String, Integer> nameToLoyaltyMap = new HashMap<String, Integer>();
        		try {
        			ResultSet rs;
        			if(label.equals("List Customers"))
        			{
        				Statement st = dbConn.createStatement();
        				rs = st.executeQuery("SELECT loyalty_number, last_name, first_name, middle_initial FROM customer ORDER BY last_name, first_name, middle_initial;");
        			}
        			else
        			{
        				String searchString = TextInputDialog.showTextInputBox(guiScreen, "Customer Search", "Enter search string", "");
        				if(searchString == null)
        					return; // user canceled
        				PreparedStatement st = dbConn.prepareStatement(
        						"SELECT loyalty_number, last_name, first_name, middle_initial " +
        						"FROM customer as c " +
        						"WHERE (to_tsvector(c.last_name) @@ plainto_tsquery('english', ?) OR " +
        						"       to_tsvector(c.first_name) @@ plainto_tsquery('english', ?));");
        				st.setString(1, searchString);
        				st.setString(2, searchString);
        				rs = st.executeQuery();
        			}
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
        				
        				nameToLoyaltyMap.put("[" + rs.getInt(1) + "]" + nameBuilder.toString(), rs.getInt(1));
        				listOptions.add("[" + rs.getInt(1) + "]" + nameBuilder.toString());
        				
        			}
        			
        			String selected = (String)ListSelectDialog.showDialog(guiScreen, "Customer List", "All customers in database", listOptions.toArray());
        			CustomerInfoWindow window = new CustomerInfoWindow(guiScreen, nameToLoyaltyMap.get(selected));
        			WindowManager.pushWindow(window);
        			guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", "Error getting list of customers" + e.getMessage());
        			return;
        		}
        	}
        	else if(label.equals("Customer Info")) {
        		String custNum = (String)TextInputDialog.showTextInputBox(guiScreen, "Customer Info", "Enter a customer loyalty number", "");
        		int loyaltyNumber = Integer.parseInt(custNum);
        		CustomerInfoWindow window = new CustomerInfoWindow(guiScreen, loyaltyNumber);
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	}
        	else {
        		MessageBox.showMessageBox(guiScreen, "Error", "Unknown option " + label);
        	}
        }
	}
	
	public static class CustomerInfoWindow extends MicrocenterWindow {
		public CustomerInfoWindow(final GUIScreen guiScreen, int loyalty_number)
		{
			super(guiScreen, "Customer Info", true);
			
			addComponent(new Button(""));
		}
	}
}
