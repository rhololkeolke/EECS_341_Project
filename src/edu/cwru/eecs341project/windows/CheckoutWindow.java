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
				WindowManager.pushWindow(window);
				guiScreen.showWindow((Window) window, GUIScreen.Position.FULL_SCREEN);
				close();
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
		public PaymentWindow(GUIScreen guiScreen, Integer shipId)
		{
			super(guiScreen, "Payment", true);
			this.shipId = shipId;
			
			mainPanel = new Panel();
			mainPanel.addComponent(new Button("Test"));
			addComponent(mainPanel);
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
