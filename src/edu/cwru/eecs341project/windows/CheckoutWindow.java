package edu.cwru.eecs341project.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.MessageBox;

import edu.cwru.eecs341project.CartItem;
import edu.cwru.eecs341project.GlobalState;

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
}
