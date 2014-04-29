package edu.cwru.eecs341project.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		public NewShippingLocation(GUIScreen guiScreen)
		{
			super(guiScreen, "New Shipping Location", true);
			
			mainPanel = new Panel();
			mainPanel.addComponent(new Button("nextPage"));
			addComponent(mainPanel);
		}
	}
}
