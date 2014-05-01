package edu.cwru.eecs341project.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
import com.googlecode.lanterna.gui.component.RadioCheckBoxList;
import com.googlecode.lanterna.gui.component.TextBox;
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
		private Panel mainPanel;
		private TextBox firstNameBox;
		private TextBox middleInitialBox;
		private TextBox lastNameBox;
		private TextBox birthdayBox;
		private RadioCheckBoxList genderRadio;
		private Label joinDateLabel;
		private Label loyaltyPointsLabel;
		private Button saveButton;
		public CustomerInfoWindow(final GUIScreen guiScreen,final int loyalty_number)
		{
			super(guiScreen, "Customer Info", true);
		
			mainPanel = new Panel();
			
			Connection dbConn = GlobalState.getDBConnection();
			try {
				PreparedStatement st = dbConn.prepareStatement(
						"SELECT first_name, middle_initial, last_name, birthdate, gender, join_date, loyalty_points " +
						"FROM customer " +
						"WHERE loyalty_number = ?;");
				st.setInt(1, loyalty_number);
				ResultSet rs = st.executeQuery();
				if(!rs.next())
				{
					rs.close();
					st.close();
					MessageBox.showMessageBox(guiScreen, "Errro", "Could not retrieve customer information");
					close();
					return;
				}
				firstNameBox = new TextBox(rs.getString(1));
				middleInitialBox = new TextBox(rs.getString(2));
				lastNameBox = new TextBox(rs.getString(3));
				birthdayBox = new TextBox(rs.getDate(4).toString());
				genderRadio = new RadioCheckBoxList();
				genderRadio.addItem("m");
				genderRadio.addItem("f");
				if(rs.getString(5).equals("m"))
					genderRadio.setCheckedItemIndex(0);
				else
					genderRadio.setCheckedItemIndex(1);
				joinDateLabel = new Label(rs.getDate(6).toString());
				loyaltyPointsLabel = new Label(""+rs.getInt(7));
				
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
				close();
				return;
			}
			
			Panel infoPanel = new Panel(Panel.Orientation.HORISONTAL);
			Panel leftPanel = new Panel();
			Panel rightPanel = new Panel();
			leftPanel.addComponent(new Label("Loyalty Number: "));
			rightPanel.addComponent(new Label("" + loyalty_number));
			leftPanel.addComponent(new Label("First Name: "));
			rightPanel.addComponent(firstNameBox);
			leftPanel.addComponent(new Label("Middle Initial: "));
			rightPanel.addComponent(middleInitialBox);
			leftPanel.addComponent(new Label("Last Name: "));
			rightPanel.addComponent(lastNameBox);
			leftPanel.addComponent(new Label("Birthday"));
			rightPanel.addComponent(birthdayBox);
			leftPanel.addComponent(new Label("Gender: "));
			leftPanel.addComponent(new Label(""));
			rightPanel.addComponent(genderRadio);
			leftPanel.addComponent(new Label("Join Date"));
			rightPanel.addComponent(joinDateLabel);
			leftPanel.addComponent(new Label("Loyalty Points"));
			rightPanel.addComponent(loyaltyPointsLabel);
			
			infoPanel.addComponent(leftPanel);
			infoPanel.addComponent(rightPanel);
			
			mainPanel.addComponent(infoPanel);
			
			mainPanel.addComponent(new Button("Save data", new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					try {
						PreparedStatement st = dbConn.prepareStatement(
								"UPDATE customer " +
								"SET first_name=?, middle_initial=?, last_name=?, birthdate=to_date(?, 'YYYY-MM-DD'), gender=? " +
								"WHERE loyalty_number = ?;");
						st.setString(1, firstNameBox.getText().trim());
						st.setString(2, middleInitialBox.getText().trim());
						st.setString(3, lastNameBox.getText().trim());
						st.setString(4, birthdayBox.getText().trim());
						st.setString(5, ((String)genderRadio.getCheckedItem()).toUpperCase());
						st.setInt(6, loyalty_number);
						st.executeUpdate();
						
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
						close();
						return;
					}
				}
			}));
			
			mainPanel.addComponent(new Button("View Orders", new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					try {
						PreparedStatement st = dbConn.prepareStatement(
								"SELECT id, order_date " +
								"FROM orders " +
								"WHERE loyalty_number = ? " +
								"ORDER BY order_date DESC;");
						st.setInt(1, loyalty_number);
						ResultSet rs = st.executeQuery();
						List<String> options = new ArrayList<String>();
						Map<String, Integer> orderIdMap = new HashMap<String, Integer>();
						while(rs.next())
						{
							String name = "["+rs.getDate(2).toString()+"]"+rs.getInt(1);
							orderIdMap.put(name, rs.getInt(1));
							options.add(name);
						}
						rs.close();
						st.close();
						
						if(options.size() == 0)
						{
							MessageBox.showMessageBox(guiScreen, "Orders", "No orders found");
							return;
						}
						
						String selected = (String)ListSelectDialog.showDialog(guiScreen, "Orders", "All orders", options.toArray());
						
						if(selected == null)
						{

							return;
						}
						
						// otherwise spawn a new order window for this order
						OrderViewWindow window = new OrderViewWindow(guiScreen, orderIdMap.get(selected));
						WindowManager.pushWindow(window);
						guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
					}
				}
			}));
			
			addComponent(mainPanel);
		}
	}
	
	public static class OrderViewWindow extends MicrocenterWindow {
		Panel mainPanel;
		List<Button> returnButtons;
		public OrderViewWindow(final GUIScreen guiScreen, final int orderId)
		{
			super(guiScreen, "Order View", true);
			
			mainPanel = new Panel();
			returnButtons = new ArrayList<Button>();
			
			Connection dbConn = GlobalState.getDBConnection();
			try {
				PreparedStatement st = dbConn.prepareStatement(
						"SELECT order_date, payment_type, shipping_loc, shipping_cost, loyalty_number " +
						"FROM orders " +
						"WHERE id = ?;");
				st.setInt(1, orderId);
				ResultSet rs = st.executeQuery();
				
				if(!rs.next())
				{
					rs.close();
					st.close();
					MessageBox.showMessageBox(guiScreen, "Error", "Could not find specified orders");
					close();
				}
				
				Panel orderInfoPanel = new Panel(Panel.Orientation.HORISONTAL);
				Panel leftInfoPanel = new Panel();
				Panel rightInfoPanel = new Panel();
				
				leftInfoPanel.addComponent(new Label("OrderId: "));
				rightInfoPanel.addComponent(new Label(""+orderId));
				leftInfoPanel.addComponent(new Label("Date: "));
				rightInfoPanel.addComponent(new Label(rs.getDate(1).toString()));
				leftInfoPanel.addComponent(new Label("Payment Method: "));
				rightInfoPanel.addComponent(new Label(rs.getString(2)));
				
				int loyaltyNumber = rs.getInt(5);
				double shippingCost = 0;
				int shippingLoc = rs.getInt(3);
				if(!rs.wasNull())
				{
					shippingCost = rs.getDouble(4);
					rs.close();
					st.close();
					
					st = dbConn.prepareStatement(
							"SELECT name " +
							"FROM shipping_location " +
							"WHERE loyalty_number = ? AND" +
							"      id = ?; ");
					st.setInt(1, loyaltyNumber);
					st.setInt(2, shippingLoc);
					rs = st.executeQuery();
					
					if(!rs.next())
					{
						rs.close();
						st.close();
						MessageBox.showMessageBox(guiScreen, "Error", "Invalid shipping location");
						close();
					}
					
					leftInfoPanel.addComponent(new Label("Shipping Address: "));
					rightInfoPanel.addComponent(new Label(rs.getString(1)));
				}
				st.close();
				rs.close();
				orderInfoPanel.addComponent(leftInfoPanel);
				orderInfoPanel.addComponent(rightInfoPanel);
				
				mainPanel.addComponent(orderInfoPanel);
				mainPanel.addComponent(new Label("")); // insert blank line
				
				// now get the order items
				Panel orderItemPanel = new Panel(Panel.Orientation.HORISONTAL);
				Panel upcPanel = new Panel();
				upcPanel.addComponent(new Label("UPC"));
				upcPanel.addComponent(new Label("----"));
				Panel namePanel = new Panel();
				namePanel.addComponent(new Label("Name"));
				namePanel.addComponent(new Label("------"));
				Panel pricePanel = new Panel();
				pricePanel.addComponent(new Label("Price"));
				pricePanel.addComponent(new Label("-------"));
				Panel quantityPanel = new Panel();
				quantityPanel.addComponent(new Label("Quantity"));
				quantityPanel.addComponent(new Label("---------"));
				Panel returnPanel = new Panel();
				returnPanel.addComponent(new Label("Return"));
				returnPanel.addComponent(new Label("-------"));
				st = dbConn.prepareStatement(
						"SELECT oi.upc, p.name, p.unit_price, oi.quantity " +
						"FROM order_item as oi, " +
						"     orders as o, " +
						"     product as p " +
						"WHERE o.id = oi.order_id AND " +
						"      o.id = ? AND " +
						"      oi.upc = p.upc;");
				st.setInt(1, orderId);
				rs = st.executeQuery();
				double totalCost = 0;
				while(rs.next())
				{
					upcPanel.addComponent(new Label(""+rs.getLong(1)));
					namePanel.addComponent(new Label(rs.getString(2)));
					pricePanel.addComponent(new Label(String.format("$%.2f", rs.getDouble(3))));
					quantityPanel.addComponent(new Label(""+rs.getInt(4)));
					
					totalCost += rs.getDouble(3)*rs.getInt(4);
					
					final int orderedQuantity = rs.getInt(4);
					final long orderedUpc = rs.getLong(1);
					Button returnButton = new Button("return", new Action() {
						@Override
						public void doAction() {
							// find all other returns for this item
							Connection dbConn = GlobalState.getDBConnection();
							PreparedStatement st1;
							try {
								// has to be sum in case split across multiple returns
								st1 = dbConn.prepareStatement(
										"SELECT SUM(ri.quantity) " +
										"FROM return_item as ri, " +
										"     orders as o " +
										"WHERE o.id = ri.order_id AND " +
										"      o.id = ? AND " +
										"      ri.upc = ?;");
								st1.setInt(1, orderId);
								st1.setLong(2, orderedUpc);
								System.out.println(st1.toString());
								ResultSet rs1 = st1.executeQuery();
								if(!rs1.next())
								{
									rs1.close();
									st1.close();
									MessageBox.showMessageBox(guiScreen, "Error", "Returned quantity query failed");
									return;
								}
								
								int returnedQuantity = rs1.getInt(1);
								if(returnedQuantity >= orderedQuantity)
								{
									MessageBox.showMessageBox(guiScreen, "Error", "You have already returned all of this item that you ordered");
									return;
								}
								
								String strQuantity = TextInputDialog.showTextInputBox(guiScreen, "Amount to Return", "Enter an amount less than or equal to " + (orderedQuantity - returnedQuantity), "");
								if(strQuantity == null || strQuantity.length() == 0)
									return;
								int quantity = Integer.parseInt(strQuantity);
								if(quantity == 0)
									return;
								if(quantity < 0)
								{
									MessageBox.showMessageBox(guiScreen, "Error", "Return amount must be positive");
									return;
								}
								if(quantity > (orderedQuantity - returnedQuantity))
								{
									MessageBox.showMessageBox(guiScreen, "Error", "Cannot return more than what was ordered");
									return;
								}
								rs1.close();
								st1.close();
								
								st1 = dbConn.prepareStatement(
										"INSERT INTO return_item " +
										"VALUES (?, ?, ?, ?);");
								st1.setInt(1, orderId);
								st1.setLong(2, orderedUpc);
								st1.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
								st1.setInt(4, quantity);
								st1.executeUpdate();
								
								close();
								OrderViewWindow window = new OrderViewWindow(guiScreen, orderId);
								WindowManager.pushWindow(window);
								guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
								
							} catch (SQLException e) {
								MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
							}
						}
					});
					returnButtons.add(returnButton);
					returnPanel.addComponent(returnButton);
				}
				st.close();
				rs.close();
				namePanel.addComponent(new Label(""));
				namePanel.addComponent(new Label("Shipping"));
				namePanel.addComponent(new Label("Sales Tax:"));
				namePanel.addComponent(new Label("Total: "));
				pricePanel.addComponent(new Label("-------"));
				pricePanel.addComponent(new Label(String.format("$%.2f", shippingCost)));
				pricePanel.addComponent(new Label(String.format("$%.2f", totalCost*.07)));
				pricePanel.addComponent(new Label(String.format("$%.2f", totalCost*1.07 + shippingCost)));
				orderItemPanel.addComponent(upcPanel);
				orderItemPanel.addComponent(namePanel);
				orderItemPanel.addComponent(pricePanel);
				orderItemPanel.addComponent(quantityPanel);
				orderItemPanel.addComponent(returnPanel);
				mainPanel.addComponent(orderItemPanel);
				
				mainPanel.addComponent(new Label(""));
				
				Panel returnItemPanel = new Panel(Panel.Orientation.HORISONTAL);
				Panel returnDatePanel = new Panel();
				returnDatePanel.addComponent(new Label("Return Date"));
				returnDatePanel.addComponent(new Label("------------"));
				Panel returnUpcPanel = new Panel();
				returnUpcPanel.addComponent(new Label("UPC"));
				returnUpcPanel.addComponent(new Label("-----"));
				Panel returnNamePanel = new Panel();
				returnNamePanel.addComponent(new Label("Name"));
				returnNamePanel.addComponent(new Label("------"));
				Panel returnQuantityPanel = new Panel();
				returnQuantityPanel.addComponent(new Label("Quantity"));
				returnQuantityPanel.addComponent(new Label("---------"));
				
				st = dbConn.prepareStatement(
						"SELECT ri.upc, ri.return_date, ri.quantity, p.name " +
						"FROM return_item as ri, " +
						"     orders as o, " +
						"     product as p " +
						"WHERE o.id = ri.order_id AND " +
						"      p.upc = ri.upc AND " +
						"      o.id = ?;");
				st.setInt(1, orderId);
				rs = st.executeQuery();
				while(rs.next())
				{
					returnDatePanel.addComponent(new Label(rs.getDate(2).toString()));
					returnUpcPanel.addComponent(new Label(""+rs.getLong(1)));
					returnNamePanel.addComponent(new Label(rs.getString(4)));
					returnQuantityPanel.addComponent(new Label(""+rs.getInt(3)));
				}
				
				returnItemPanel.addComponent(returnDatePanel);
				returnItemPanel.addComponent(returnUpcPanel);
				returnItemPanel.addComponent(returnNamePanel);
				returnItemPanel.addComponent(returnQuantityPanel);
				mainPanel.addComponent(returnItemPanel);
				rs.close();
				st.close();
				
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
			}
			
			addComponent(mainPanel);
		}
	}
}
