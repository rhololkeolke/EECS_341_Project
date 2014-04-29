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
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.RadioCheckBoxList;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;

import edu.cwru.eecs341project.CartItem;
import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

public class CheckoutWindow extends MicrocenterWindow{	

	private GUIScreen guiScreen;
	private Panel mainPanel;
	private Panel cartPanel;
	private List<TextBox> quantityBoxes;
	private List<CartItem> items;
	public CheckoutWindow(final GUIScreen guiScreen)
	{
		super(guiScreen, "Shopping Cart", true);
		this.guiScreen = guiScreen;
		
		
		drawWindow(guiScreen);
		
	}
	
	private void drawWindow(final GUIScreen guiScreen) {
		mainPanel = new Panel();
		cartPanel = new Panel(Panel.Orientation.HORISONTAL);
		quantityBoxes = new ArrayList<TextBox>();
		items = GlobalState.getCartItems();
		
		Panel leftPanel = new Panel();
		leftPanel.addComponent(new Label("Items"));
		leftPanel.addComponent(new Label("------"));
		Panel middlePanel = new Panel();
		middlePanel.addComponent(new Label("Price"));
		middlePanel.addComponent(new Label("------"));
		Panel rightPanel = new Panel();
		rightPanel.addComponent(new Label("Quantity"));
		rightPanel.addComponent(new Label("---------"));
		for(CartItem item : items)
		{
			leftPanel.addComponent(new Label("[" + item.storeId + "][" + item.upc + "] " + item.name));
			middlePanel.addComponent(new Label("$"+item.price));
			TextBox quantityBox = new TextBox(""+item.getQuantity());
			quantityBoxes.add(quantityBox);
			rightPanel.addComponent(quantityBox);	
		}
		leftPanel.addComponent(new Label(""));
		middlePanel.addComponent(new Label("--------"));
		leftPanel.addComponent(new Label("Total:"));
		double totalCost = 0;
		for(CartItem item : items)
		{
			totalCost += item.price*item.getQuantity();
		}
		middlePanel.addComponent(new Label(String.format("$%.2f", totalCost)));
		cartPanel.addComponent(leftPanel);
		cartPanel.addComponent(middlePanel);
		cartPanel.addComponent(rightPanel);
		
		mainPanel.addComponent(cartPanel);
		
		Panel buttonsPanel = new Panel(Panel.Orientation.HORISONTAL);
		buttonsPanel.addComponent(new Button("Save Cart", new Action() {
			@Override
			public void doAction() {
				Connection dbConn = GlobalState.getDBConnection();
				for(int i=0; i<quantityBoxes.size(); i++)
				{
					Integer newQuantity = Integer.parseInt(quantityBoxes.get(i).getText());
					if(newQuantity < 0)
					{
						MessageBox.showMessageBox(guiScreen, "Error", "Quantities must be positive");
						return;
					}
					CartItem currItem = items.get(i);
					if(newQuantity == 0)
					{
						GlobalState.removeCartItem(items.get(i));
					}
					else if(newQuantity < currItem.getQuantity())
					{
						GlobalState.removeCartItem(currItem);
						try {
							currItem.setQuantity(newQuantity);
							GlobalState.addCartItem(currItem);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else
					{
						try{
							PreparedStatement st = dbConn.prepareCall(
									"SELECT amount " +
									"FROM stock " +
									"WHERE store_id = ? AND " +
									"      upc = ?;");
							st.setInt(1, currItem.storeId);
							st.setLong(2, currItem.upc);
							ResultSet rs = st.executeQuery();
							if(!rs.next())
							{
								MessageBox.showMessageBox(guiScreen, "Error", "Could not find product " + currItem.upc + " at store " + currItem.storeName);
								return;
							}
							int availQuantity = rs.getInt(1);
							if(newQuantity > availQuantity)
							{
								MessageBox.showMessageBox(guiScreen, "Error", currItem.storeName + " does not have sufficient stock. Please check other stores before giving up");
								return;
							}
							
							currItem.setQuantity(newQuantity);
							GlobalState.removeCartItem(currItem);
							GlobalState.addCartItem(currItem);
						} catch(SQLException e) {
							MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
							System.out.println(e.getMessage());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				refresh();
			}
		}));
		buttonsPanel.addComponent(new Button("Checkout", new Action() {
			@Override
			public void doAction() {
				DialogResult result = MessageBox.showMessageBox(guiScreen, "Delivery or Pickup", "Is this a delivery package?", DialogButtons.YES_NO);
				ManagedWindow window;
				if(result == DialogResult.YES)
				{
					// spawn the ship window
					window = new ShippingLocations(guiScreen);
				}
				else {
					// spawn the payment window
					window = new PaymentWindow(guiScreen, null);
				}
				close();
				WindowManager.pushWindow(window);
				guiScreen.showWindow((Window) window, GUIScreen.Position.FULL_SCREEN);
			}
		}));
		
		mainPanel.addComponent(buttonsPanel);
		addComponent(mainPanel);
	}
	
	@Override
	public void refresh() {
		super.removeComponent(mainPanel);
		drawWindow(guiScreen);
	}
	
	public class PaymentWindow extends MicrocenterWindow {
		private Panel mainPanel;
		private Integer shipId;
		private String paymentType;
		private Button paymentTypeButton;
		private Button cardTypeButton = null;
		private TextBox cashAmountBox = null;
		private Panel paymentPanel;
		private final String[] deliveryPaymentTypes = {"gift card", "credit card"};
		private final String[] allPaymentTypes = {"gift card", "credit card", "check", "money order", "cash"};
		public PaymentWindow(final GUIScreen guiScreen, final Integer shipId)
		{
			super(guiScreen, "Payment", true);
			this.shipId = shipId;
			
			mainPanel = new Panel();
			
			double itemCost = getItemCost();
			double shipCost = 0;
			if(shipId != null)
				shipCost = 10.0;
			
			Panel costPanel = new Panel(Panel.Orientation.HORISONTAL);
			Panel leftPanel = new Panel();
			Panel rightPanel = new Panel();
			leftPanel.addComponent(new Label("Cost: "));
			rightPanel.addComponent(new Label(String.format("$%.2f", itemCost)));
			leftPanel.addComponent(new Label("Shipping: "));
			rightPanel.addComponent(new Label(String.format("$%.2f", shipCost)));
			leftPanel.addComponent(new Label("Tax: "));
			rightPanel.addComponent(new Label(String.format("$%.2f", itemCost*.07)));
			leftPanel.addComponent(new Label(""));
			rightPanel.addComponent(new Label("---------"));
			leftPanel.addComponent(new Label("Total: "));
			rightPanel.addComponent(new Label(String.format("$%.2f", itemCost*1.07 + shipCost)));
			costPanel.addComponent(leftPanel);
			costPanel.addComponent(rightPanel);
			mainPanel.addComponent(costPanel);
			
			cardTypeButton = new Button("Mastercard", new Action() {
				@Override
				public void doAction() {
					String[] options = {"mastercard", "visa", "american express", "discover"};
					String selected = (String)ListSelectDialog.showDialog(guiScreen, "Card Type", "Select card type", options);
					if(selected == null)
						return;
					cardTypeButton.setText(selected);
				}
			});
			
			paymentTypeButton = new Button("Credit Card", new Action() {
				@Override
				public void doAction() {
					if(shipId != null)
						paymentType = (String)ListSelectDialog.showDialog(guiScreen, "Payment Type", "Select payment type", deliveryPaymentTypes);
					else
						paymentType = (String)ListSelectDialog.showDialog(guiScreen, "Payment Type", "Select payment type", allPaymentTypes);
					if(paymentType == null)
					{
						paymentType = "credit card";
						return; // user canceled
					}
					
					if(paymentType.equals("credit card"))
					{
						paymentPanel.removeAllComponents();
						cashAmountBox = null;
						paymentPanel.addComponent(creditCardPanel());
					}
					else if(paymentType.equals("cash"))
					{
						paymentPanel.removeAllComponents();
						cardTypeButton = null;
						paymentPanel.addComponent(cashPanel());
					}
					else
					{
						paymentPanel.removeAllComponents();
					}
				}
			});
			mainPanel.addComponent(paymentTypeButton);
			
			paymentPanel = new Panel();
			paymentPanel.addComponent(creditCardPanel());
			mainPanel.addComponent(paymentPanel);
			
			mainPanel.addComponent(new Button("Complete Purchase", new Action() {
				@Override
				public void doAction() {
					if(cashAmountBox != null)
					{
						double cashAmount = Double.parseDouble(cashAmountBox.getText());
						double change;
						if(shipId == null)
							change = cashAmount - (getItemCost()*1.07);
						else
							change = cashAmount - (getItemCost()*1.07 + 10);
						
						if(change < 0)
						{
							MessageBox.showMessageBox(guiScreen, "Error", "Cash amount provided is not enough");
							return;
						}
						else
						{
							MessageBox.showMessageBox(guiScreen, "Change", String.format("$%.2f", change));
						}
					}
					
					Connection dbConn = GlobalState.getDBConnection();
					try {
						dbConn.setAutoCommit(false);
					} catch (SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
						return;
					}
					try {
						List<CartItem> items = GlobalState.getCartItems();
						int loyalty_number;
						if(GlobalState.getUserRole() == GlobalState.UserRole.CUSTOMER)
							loyalty_number = GlobalState.getCustomerNumber();
						else
							loyalty_number = GlobalState.anonymousCustNum;
						
						PreparedStatement st = dbConn.prepareStatement(
								"INSERT INTO orders(order_date, store_id, loyalty_number, payment_type, shipping_loc, shipping_cost) " +
								"VALUES (now(), ?, ?, ?, ?, ?);", new String[] {"id"});
						st.setInt(1, items.get(0).storeId); // I know this will exist because can't get to checkout with an empty cart
						st.setInt(2, loyalty_number);
						st.setString(3, paymentTypeButton.getText());
						if(shipId != null)
						{
							st.setInt(4, shipId);
							st.setDouble(5, 10.0);
						}
						else
						{
							st.setNull(4, java.sql.Types.INTEGER);
							st.setNull(5, java.sql.Types.NUMERIC);
						}
						
						if(st.executeUpdate() <= 0)
						{
							dbConn.rollback();
							st.close();
							MessageBox.showMessageBox(guiScreen, "Checkout Error", "Failed to create order");
							return;
						}
						ResultSet rs = st.getGeneratedKeys();
						if(!rs.next())
						{
							dbConn.rollback();
							rs.close();
							st.close();
							MessageBox.showMessageBox(guiScreen, "Checkout Error", "Failed to create order");
							return;
						}
						int orderId = rs.getInt(1);
						rs.close();
						st.close();
						
						st = dbConn.prepareStatement(
								"INSERT INTO order_item(order_id, upc, quantity, discount) " +
								"VALUES (?, ?, ?, 0);");
						st.setInt(1, orderId);
						for(CartItem item : items)
						{
							st.setLong(2, item.upc);
							st.setInt(3, item.getQuantity());
							if(st.executeUpdate() <= 0)
							{
								dbConn.rollback();
								st.close();
								MessageBox.showMessageBox(guiScreen, "Checkout Error", "Could not add product " + item.upc);
								return;
							}
						}
						st.close();
						
						for(CartItem item : items)
						{
							st = dbConn.prepareStatement(
									"SELECT pl.shelf_id, s.store_id, pl.upc, pl.amount " +
									"FROM shelf as s, " +
									"     product_location as pl " +
									"WHERE s.store_id = ? AND " +
									"      pl.upc = ? AND " +
									"      s.id = pl.shelf_id;");
							st.setInt(1, item.storeId);
							st.setLong(2, item.upc);
							rs = st.executeQuery();
							for(int i=0; i<item.getQuantity(); i++)
							{
								rs.next();
								int amountToRemove = 0;
								for(int j=0; j<rs.getInt(4) && j < item.getQuantity(); j++)
								{
									amountToRemove++;
									i++;
								}
								if(amountToRemove == rs.getInt(4))
								{
									PreparedStatement st1 = dbConn.prepareStatement(
												"DELETE FROM product_location " +
												"WHERE shelf_id = ? AND" +
												"      upc = ?;");
									st1.setInt(1, rs.getInt(1));
									st1.setLong(2, item.upc);
									if(st1.executeUpdate() <= 0)
									{
										dbConn.rollback();
										rs.close();
										st.close();
										st1.close();
										MessageBox.showMessageBox(guiScreen, "Checkout Error", "Could not delete product locations");
										return;
									}
									st1.close();
								}
								else
								{
									PreparedStatement st1 = dbConn.prepareStatement(
												"UPDATE product_location " +
												"SET amount=? " +
												"WHERE shelf_id = ? AND" +
												"      upc = ?;");
									st1.setInt(1, amountToRemove);
									st1.setInt(2, rs.getInt(1));
									st1.setLong(3, item.upc);
									if(st1.executeUpdate() <= 0)
									{
										dbConn.rollback();
										rs.close();
										st.close();
										st1.close();
										MessageBox.showMessageBox(guiScreen, "Checkout Error", "Could not update product locations");
										return;
									}
									st1.close();
								}
							}
						}
						
						dbConn.commit();
						
						GlobalState.clearCart();
						
						close();
						WindowManager.refreshAllWindows();
						MessageBox.showMessageBox(guiScreen, "Checkout", "Succesfully ordered products! Your order number is " + orderId);
						return;
						
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
						try {
							dbConn.rollback();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						return;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							dbConn.setAutoCommit(true);
						} catch (SQLException e) {
							MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
							return;
						}
					}
				}
			}));
			addComponent(mainPanel);
		}
		
		private double getItemCost()
		{
			double itemCost = 0;
			for(CartItem item : GlobalState.getCartItems())
			{
				itemCost += item.price*item.getQuantity();
			}
			
			return itemCost;
		}
		
		private Panel creditCardPanel() {
			Panel creditPanel = new Panel(Panel.Orientation.HORISONTAL);
			Panel leftPanel = new Panel();
			Panel rightPanel = new Panel();
			
			leftPanel.addComponent(new Label("Card Type: "));
			rightPanel.addComponent(cardTypeButton);
			leftPanel.addComponent(new Label("Cardholder Name: "));
			rightPanel.addComponent(new TextBox(""));
			leftPanel.addComponent(new Label("Card number: "));
			rightPanel.addComponent(new TextBox(""));
			leftPanel.addComponent(new Label("Exp. Date: "));
			rightPanel.addComponent(new TextBox(""));
			leftPanel.addComponent(new Label("CCV: "));
			rightPanel.addComponent(new TextBox(""));
			
			creditPanel.addComponent(leftPanel);
			creditPanel.addComponent(rightPanel);
			return creditPanel;
		}
		
		private Panel cashPanel() {
			Panel cashPanel = new Panel(Panel.Orientation.HORISONTAL);
			cashPanel.addComponent(new Label("Amount: "));
			cashAmountBox = new TextBox("");
			cashPanel.addComponent(cashAmountBox);
			return cashPanel;
		}
	}
	
	public class ShippingLocations extends MicrocenterWindow {
		private Panel mainPanel;
		private RadioCheckBoxList locations;
		Map<String, Integer> shipLocs;
		public ShippingLocations(final GUIScreen guiScreen)
		{
			super(guiScreen, "Shipping", true);
			
			shipLocs = new HashMap<String, Integer>();
			
			mainPanel = new Panel();
			if(GlobalState.getUserRole() == GlobalState.UserRole.CUSTOMER)
			{
				Connection dbConn = GlobalState.getDBConnection();
				try {
					PreparedStatement st = dbConn.prepareStatement(
							"SELECT sl.id, sl.name " +
							"FROM shipping_location as sl, " +
							"     customer as c " +
							"WHERE c.loyalty_number = sl.loyalty_number AND " +
							"      c.loyalty_number = ?;");
					st.setInt(1, GlobalState.getCustomerNumber());
					ResultSet rs = st.executeQuery();
					while(rs.next())
					{
						shipLocs.put(rs.getString(2), rs.getInt(1));
					}
				} catch(SQLException e) {
					MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			locations = new RadioCheckBoxList();
			for(String name : shipLocs.keySet())
			{
				locations.addItem(name);
			}
			locations.addItem("(New Shipping Location)");
			locations.setCheckedItemIndex(0);
			mainPanel.addComponent(locations);
			
			mainPanel.addComponent(new Button("Next Page", new Action() {
				@Override
				public void doAction() {
					String selected = (String)locations.getCheckedItem();
					Integer locId = shipLocs.get(selected);
					
					ManagedWindow window;
					if(locId == null)
					{
						window = new NewShippingLocation(guiScreen);
					}
					else
					{
						window = new PaymentWindow(guiScreen, locId);
					}
					WindowManager.pushWindow(window);
					guiScreen.showWindow((Window) window, GUIScreen.Position.FULL_SCREEN);
					close();
				}
			}));
			addComponent(mainPanel);
		}
	}
	
	public class NewShippingLocation extends MicrocenterWindow {
		private Panel mainPanel;
		private TextBox nameBox, street1Box, street2Box, cityBox, stateBox, zipBox;
		public NewShippingLocation(final GUIScreen guiScreen)
		{
			super(guiScreen, "New Shipping Location", true);
			
			mainPanel = new Panel();
			
			Panel infoPanel = new Panel(Panel.Orientation.HORISONTAL);
			Panel leftPanel = new Panel();
			Panel rightPanel = new Panel();
			
			leftPanel.addComponent(new Label("Location name: "));
			nameBox = new TextBox();
			rightPanel.addComponent(nameBox);
			
			leftPanel.addComponent(new Label("Street 1: "));
			street1Box = new TextBox();
			rightPanel.addComponent(street1Box);
			
			leftPanel.addComponent(new Label("Street 2: "));
			street2Box = new TextBox();
			rightPanel.addComponent(street2Box);
			
			leftPanel.addComponent(new Label("City: "));
			cityBox = new TextBox();
			rightPanel.addComponent(cityBox);
			
			leftPanel.addComponent(new Label("State: "));
			stateBox = new TextBox();
			rightPanel.addComponent(stateBox);
			
			leftPanel.addComponent(new Label("Zip: "));
			zipBox = new TextBox();
			rightPanel.addComponent(zipBox);
			
			infoPanel.addComponent(leftPanel);
			infoPanel.addComponent(rightPanel);
			mainPanel.addComponent(infoPanel);
			
			mainPanel.addComponent(new Button("nextPage", new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					if(nameBox.getText().trim().length() == 0)
					{
						MessageBox.showMessageBox(guiScreen, "Error", "Shipping Location name cannot be blank");
						return;
					}
					if(street1Box.getText().trim().length() == 0)
					{
						MessageBox.showMessageBox(guiScreen, "Error", "Street1 cannot be blank");
						return;
					}
					if(cityBox.getText().trim().length() == 0)
					{
						MessageBox.showMessageBox(guiScreen, "Error", "City cannot be blank");
						return;
					}
					if(stateBox.getText().trim().length() != 2)
					{
						MessageBox.showMessageBox(guiScreen, "Error", "State must be a two letter abbreviation");
						return;
					}
					if(zipBox.getText().trim().length() == 0)
					{
						MessageBox.showMessageBox(guiScreen, "Error", "Zip cannot be blank");
						return;
					}
					try {
						String[] id_col = {"id"};
						PreparedStatement st = dbConn.prepareStatement(
								"INSERT INTO shipping_location(loyalty_number, name, street1, street2, city, state, zip) " +
								"VALUES (?, ?, ?, ?, ?, ?, ?);", id_col);
						int loyalty_number;
						if(GlobalState.getUserRole() == GlobalState.UserRole.CUSTOMER)
						{
							loyalty_number = GlobalState.getCustomerNumber();
						}
						else
						{
							loyalty_number = GlobalState.anonymousCustNum;
						}
						st.setInt(1, loyalty_number);
						st.setString(2, nameBox.getText().trim());
						st.setString(3, street1Box.getText().trim());
						if(street2Box.getText().trim().length() == 0)
						{
							st.setString(4, null);
						}
						else
						{
							st.setString(4, street2Box.getText().trim());
						}
						st.setString(5, cityBox.getText().trim());
						st.setString(6, stateBox.getText().trim());
						st.setInt(7, Integer.parseInt(zipBox.getText().trim()));
						if(st.executeUpdate() <= 0)
						{
							st.close();
							MessageBox.showMessageBox(guiScreen, "Error", "Could not create shipping location");
							return;
						}
						st.close();
												
						st = dbConn.prepareStatement(
								"SELECT sl.id " +
								"FROM shipping_location as sl " +
								"WHERE loyalty_number = ? AND " +
								"      name = ? " +
								"LIMIT 1;");
						st.setInt(1, loyalty_number);
						st.setString(2, nameBox.getText().trim());
						ResultSet rs = st.executeQuery();
						
						if(!rs.next())
						{
							MessageBox.showMessageBox(guiScreen, "Error", "Could not create shipping location");
							return;
						}
						
						Integer shipId = rs.getInt(1);
						rs.close();
						st.close();
						
						close();
						PaymentWindow window = new PaymentWindow(guiScreen, shipId);
						WindowManager.pushWindow(window);
						guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);

					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}));
			addComponent(mainPanel);
		}
	}
}
