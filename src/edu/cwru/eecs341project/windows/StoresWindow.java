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
		public StoreInfoWindow(final GUIScreen guiScreen, final int storeId)
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
				
				infoPanel.addComponent(leftPanel);
				infoPanel.addComponent(rightPanel);
				mainPanel.addComponent(infoPanel);
				
				rs.close();
				st.close();
				st = dbConn.prepareStatement(
						"SELECT day_of_week, open_hour, close_hour " +
						"FROM store_hours " +
						"WHERE store_id = ?;");
				st.setInt(1, storeId);
				rs = st.executeQuery();
				
				Map<String, java.sql.Time> openMap = new HashMap<String, java.sql.Time>();
				Map<String, java.sql.Time> closeMap = new HashMap<String, java.sql.Time>();
				while(rs.next())
				{
					openMap.put(rs.getString(1), rs.getTime(2));
					closeMap.put(rs.getString(1), rs.getTime(3));
				}
				rs.close();
				st.close();
				Panel hoursPanel = new Panel(Panel.Orientation.HORISONTAL);
				Panel dayPanel = new Panel();
				dayPanel.addComponent(new Label("Day"));
				dayPanel.addComponent(new Label("----"));
				Panel openPanel = new Panel();
				openPanel.addComponent(new Label("Open"));
				openPanel.addComponent(new Label("------"));
				Panel closePanel = new Panel();
				closePanel.addComponent(new Label("Close"));
				closePanel.addComponent(new Label("------"));
				
				dayPanel.addComponent(new Label("Sunday"));
				java.sql.Time openHour = openMap.get("Su");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				java.sql.Time closeHour = closeMap.get("Su");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				dayPanel.addComponent(new Label("Monday"));
				openHour = openMap.get("M");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				closeHour = closeMap.get("M");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				dayPanel.addComponent(new Label("Tuesday"));
				openHour = openMap.get("Tu");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				closeHour = closeMap.get("Tu");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				dayPanel.addComponent(new Label("Wednesday"));
				openHour = openMap.get("W");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				closeHour = closeMap.get("W");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				dayPanel.addComponent(new Label("Thursday"));
				openHour = openMap.get("Th");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				closeHour = closeMap.get("Th");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				dayPanel.addComponent(new Label("Friday"));
				openHour = openMap.get("F");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				closeHour = closeMap.get("F");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				dayPanel.addComponent(new Label("Saturday"));
				openHour = openMap.get("Sa");
				if(openHour != null)
					openPanel.addComponent(new Label(openHour.toString()));
				else
					openPanel.addComponent(new Label("----"));
				closeHour = closeMap.get("Sa");
				if(closeHour != null)
					closePanel.addComponent(new Label(closeHour.toString()));
				else
					closePanel.addComponent(new Label("------"));
				
				hoursPanel.addComponent(dayPanel);
				hoursPanel.addComponent(openPanel);
				hoursPanel.addComponent(closePanel);
				mainPanel.addComponent(hoursPanel);
				
				// get any closings within 1 week of today
				st = dbConn.prepareStatement(
						"SELECT s.closed_date, s.desc " +
						"FROM store_closing as s " +
						"WHERE s.store_id = ? AND " +
						"      s.closed_date BETWEEN NOW() AND NOW() + '7 days'::interval;");
				st.setInt(1, storeId);
				rs = st.executeQuery();
				
				if(rs.next())
				{
					mainPanel.addComponent(new Label(""));
					mainPanel.addComponent(new Label("Closed: " + rs.getDate(1).toString() + " Reason: " + rs.getString(2)));
				}
				
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
			}

			
			mainPanel.addComponent(new Button("View Product Stock", new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					PreparedStatement st;
					try {
						st = dbConn.prepareStatement(
								"SELECT p.upc, p.name, s.amount, p.unit_price " +
								"FROM product as p, " +
								"     stock as s " +
								"WHERE p.upc = s.upc AND " +
								"      s.store_id = ? " +
								"ORDER BY s.amount;");
						st.setInt(1, storeId);
						ResultSet rs = st.executeQuery();
						
						List<String> options = new ArrayList<String>();
						Map<String, Long> upcMap = new HashMap<String, Long>();
						Map<String, Double> priceMap = new HashMap<String, Double>();
						while(rs.next())
						{
							String name = "["+rs.getLong(1)+"] "+rs.getString(2)+" | " + rs.getInt(3);
							options.add(name);
							upcMap.put(name, rs.getLong(1));
							priceMap.put(name, rs.getDouble(4));
						}
						rs.close();
						st.close();
						
						String selected = (String)ListSelectDialog.showDialog(guiScreen, "Products", "List of products sorted by stocked amount", options.toArray());
						if(selected == null)
							return;
						
						if(GlobalState.getUserRole() == GlobalState.UserRole.EMPLOYEE || GlobalState.getUserRole() == GlobalState.UserRole.DBA)
						{
							Long upc = upcMap.get(selected);
							Double price = priceMap.get(selected);
							// display a list of all vendors selling this item
							st = dbConn.prepareStatement(
									"SELECT v.id, v.name " +
									"FROM product as p, " +
									"     vendor as v, " +
									"     supplies as s " +
									"WHERE p.upc = ? AND " +
									"      p.brand = s.brand_id AND " +
									"      s.vendor_id = v.id;");
							st.setLong(1, upc);
							rs = st.executeQuery();
							
							options.clear();
							Map<String, Integer> vidMap = new HashMap<String, Integer>();
							while(rs.next())
							{
								String name = "["+rs.getInt(1)+"] " + rs.getString(2);
								options.add(name);
								vidMap.put(name, rs.getInt(1));
							}
							rs.close();
							st.close();
							
							selected = (String)ListSelectDialog.showDialog(guiScreen, "Vendor Select", "Select vendor to purchase from", options.toArray());
							if(selected == null)
								return;
							
							String strAmount = TextInputDialog.showTextInputBox(guiScreen, "Purchase Amount", "How many do you want to order", "");
							if(strAmount == null || strAmount.length() == 0)
								return;
							
							int amount = Integer.parseInt(strAmount);
							if(amount < 0)
							{
								MessageBox.showMessageBox(guiScreen, "Error", "Amount must be positive");
								return;
							}
							st = dbConn.prepareStatement(
									"INSERT INTO vendor_purchase " +
									"VALUES (?, ?, ?, NOW(), ?, ?);");
							st.setInt(1, storeId);
							st.setInt(2, vidMap.get(selected));
							st.setLong(3, upc);
							st.setInt(4, amount);
							st.setDouble(5, price*.9);
							st.executeUpdate();
							
							MessageBox.showMessageBox(guiScreen, "Success", "Successfully ordered more product");
						}
					} catch (SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
					}
				}
			}));
			
			addComponent(mainPanel);
		}
	}
}
