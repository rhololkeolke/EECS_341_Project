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
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;

import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

public class ProductsWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;

	public ProductsWindow(GUIScreen guiScreen) {
		super(guiScreen, "Products", true, false);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem("Search"));
		actionListBox.addAction(new ActionListBoxItem("Browse"));
		actionListBox.addAction(new ActionListBoxItem("Product Info"));
		actionListBox.addAction(new ActionListBoxItem("Compare Products"));
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
        	if(label.equals("Search")) {
        		String searchString = null;
        		while(searchString == null)
        		{
        			searchString = TextInputDialog.showTextInputBox(guiScreen, "Product Search", "Enter Search", "");
        			if(searchString == null)
        				return; // user canceled
        			if(searchString.trim().length() == 0)
        			{
        				MessageBox.showMessageBox(guiScreen, "Error", "Please enter a non empty search");
        				searchString = null;
        			}
        		}
        		
        		Connection dbConn = GlobalState.getDBConnection();
        		try{
        			PreparedStatement st = dbConn.prepareStatement("SELECT p.upc, substring(p.name for 27), b.name, substring(p.desc for 27) " +
        					"FROM product as p, " +
        					"     brand as b " +
        					"WHERE p.brand = b.id AND" +
        					"      (to_tsvector(p.name) @@ plainto_tsquery('english', ?) OR " +
        					"       to_tsvector(p.desc) @@ plainto_tsquery('english', ?) OR " +
        					"       to_tsvector(b.name) @@ plainto_tsquery('english', ?));");
        			st.setString(1, searchString);
        			st.setString(2, searchString);
        			st.setString(3, searchString);
        			ResultSet rs = st.executeQuery();
        			
        			List<String> searchResults = new ArrayList<String>(); // list that is displayed
        			Map<String, Long> searchResultsMap = new HashMap<String, Long>(); // makes it easy to look up the upc
        			
        			while(rs.next())
        			{
        				StringBuilder displayName = new StringBuilder();
        				displayName.append(rs.getString(1));
        				displayName.append(" | ");
        				displayName.append(rs.getString(2));
        				displayName.append(" | ");
        				displayName.append(rs.getString(3));
        				displayName.append(" | ");
        				displayName.append(rs.getString(4));
        				searchResults.add(displayName.toString());
        				
        				searchResultsMap.put(displayName.toString(), rs.getLong(1));
        			}
        			
        			rs.close();
        			st.close();
        			
        			if(searchResults.size() == 0)
        			{
        				MessageBox.showMessageBox(guiScreen, "Search Results", "No Results found");
        				return;
        			}
        			String selectedProduct = (String)ListSelectDialog.showDialog(guiScreen, "Search Results", "Found " + searchResults.size() + " matches", searchResults.toArray());
        			if(selectedProduct == null)
        				return; // user canceled
        			ProductInfoWindow window = new ProductInfoWindow(guiScreen, searchResultsMap.get(selectedProduct));
        			WindowManager.pushWindow(window);
        			guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
        			System.out.println(e.getMessage());
        		}
        	} else if(label.equals("Product Info")) {
        		String upcString = TextInputDialog.showTextInputBox(guiScreen, "Product UPC", "Enter the UPC of the product you want to see informaiton about", "");
        		if(upcString == null)
        			return; // user canceled
        		ProductInfoWindow window = new ProductInfoWindow(guiScreen, Long.parseLong(upcString));
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	} else {
        		MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        	}
        }
	}
	
	private class ProductInfoWindow extends MicrocenterWindow
	{
		private final Long upc;
		private String name;
		private String desc;
		private double unitPrice;
		private String brand;
		private List<String> categories;
		private int currStore;
		private String currStoreName = "Website";
		private int stock;
		
		private Panel mainPanel;
		private TextBox  nameBox;
		private TextArea descArea;
		private TextBox unitPriceBox;
		private Button brandButton;
		private Button storeButton;
		public ProductInfoWindow(final GUIScreen guiScreen, Long upc)
		{
			super(guiScreen, "Product Info", true, false);
			this.upc = upc;
			
			Connection dbConn = GlobalState.getDBConnection();
			try {
				PreparedStatement st = dbConn.prepareStatement("SELECT p.name, p.desc, p.unit_price, b.name " +
						"FROM product as p, " +
						"     brand as b " +
						"WHERE p.brand = b.id AND " +
						"      p.upc = ?;");
				st.setLong(1, upc);
				ResultSet rs = st.executeQuery();
				if(!rs.next())
				{
					MessageBox.showMessageBox(guiScreen, "Error", "Could not find product " + upc);
					close();
					return;
				}
				name = rs.getString(1);
				desc = rs.getString(2);
				unitPrice = rs.getDouble(3);
				brand = rs.getString(4);
				
				rs.close();
				st.close();
				
				// get the category hierarchy
				st = dbConn.prepareStatement("SELECT parent.name " +
						"FROM product_type_tree as node, " +
						"     product_type_tree as parent, " +
						"     product_type as pt " +
						"WHERE pt.id = node.id AND " +
						"      pt.upc = ? AND " +
						"      node.lft BETWEEN parent.lft AND parent.rgt " +
						"ORDER BY parent.lft;");
				st.setLong(1, upc);
				rs = st.executeQuery();
				
				categories = new ArrayList<String>();
				while(rs.next())
				{
					categories.add(rs.getString(1));
				}
				
				rs.close();
				st.close();
				
				// get the stock information for this product
				st = dbConn.prepareStatement("SELECT s.id, st.amount " +
						"FROM stock st, " +
						"     store as s " +
						"WHERE st.store_id = s.id AND " +
						"      s.name = ? AND " +
						"      st.upc = ?;");
				st.setString(1, currStoreName);
				st.setLong(2, upc);
				rs = st.executeQuery();
				
				if(!rs.next())
				{
					MessageBox.showMessageBox(guiScreen, "Error", "Could not get stock information");
					close();
					return;
				}
				
				currStore = rs.getInt(1);
				stock = rs.getInt(2);
				
				rs.close();
				st.close();
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
				System.out.println(e.getMessage());
			}
			
			mainPanel = new Panel();
			
			Panel categoryPanel = new Panel(Panel.Orientation.HORISONTAL);
			for(String cat : categories)
			{
				categoryPanel.addComponent(new Button(cat, new Action() {
					@Override
					public void doAction() {
						MessageBox.showMessageBox(guiScreen, "TODO", "TODO");
					}
				}));
			}
			mainPanel.addComponent(categoryPanel);
			
			Panel upcPanel = new Panel(Panel.Orientation.HORISONTAL);
			upcPanel.addComponent(new Label("UPC: "));
			upcPanel.addComponent(new Label(""+upc));
			mainPanel.addComponent(upcPanel);
			
			Panel namePanel = new Panel(Panel.Orientation.HORISONTAL);
			namePanel.addComponent(new Label("Name: "));
			if(GlobalState.getUserRole() == GlobalState.UserRole.DBA || GlobalState.getUserRole() == GlobalState.UserRole.EMPLOYEE)
			{
				nameBox = new TextBox(name);
				namePanel.addComponent(nameBox);
			}
			else
			{
				namePanel.addComponent(new Label(name));
			}
			mainPanel.addComponent(namePanel);
			
			Panel brandPanel = new Panel(Panel.Orientation.HORISONTAL);
			brandPanel.addComponent(new Label("Brand: "));
			if(GlobalState.getUserRole() == GlobalState.UserRole.DBA || GlobalState.getUserRole() == GlobalState.UserRole.EMPLOYEE)
			{
				brandButton = new Button(brand, new Action() {
					@Override
					public void doAction() {
						MessageBox.showMessageBox(guiScreen, "TODO", "TODO");
					}
				});
				brandPanel.addComponent(brandButton);
			}
			else
			{
				brandPanel.addComponent(new Label(brand));
			}
			mainPanel.addComponent(brandPanel);
			
			Panel descPanel = new Panel(Panel.Orientation.HORISONTAL);
			descPanel.addComponent(new Label("Description: "));
			String[] descStrings = desc.split("[.!]");
			StringBuilder newlineDesc = new StringBuilder();
			for(String line : descStrings)
			{
				newlineDesc.append(line);
				newlineDesc.append("\n");
			}
			descArea = new TextArea(newlineDesc.toString());
			descPanel.addComponent(descArea);
			mainPanel.addComponent(descPanel);
			
			Panel pricePanel = new Panel(Panel.Orientation.HORISONTAL);
			pricePanel.addComponent(new Label("Price: $"));
			if(GlobalState.getUserRole() == GlobalState.UserRole.DBA || GlobalState.getUserRole() == GlobalState.UserRole.EMPLOYEE)
			{
				unitPriceBox = new TextBox(""+unitPrice);
				pricePanel.addComponent(unitPriceBox);
			}
			else
			{
				pricePanel.addComponent(new Label(""+unitPrice));
			}
			mainPanel.addComponent(pricePanel);

			mainPanel.addComponent(new Button("View Specs"));
			
			Panel stockPanel = new Panel(Panel.Orientation.HORISONTAL);
			storeButton = new Button(currStoreName);
			stockPanel.addComponent(storeButton);
			stockPanel.addComponent(new Label(" stock: "));
			stockPanel.addComponent(new Label("" + stock));
			mainPanel.addComponent(stockPanel);
			
			Panel buttonsPanel = new Panel(Panel.Orientation.HORISONTAL);
			Button addToCartButton = new Button("Add to Cart");
			buttonsPanel.addComponent(addToCartButton);
			Button saveButton = new Button("Save Changes");
			buttonsPanel.addComponent(saveButton);
			mainPanel.addComponent(buttonsPanel);
			
			addComponent(mainPanel);
		}
		
	}

}
