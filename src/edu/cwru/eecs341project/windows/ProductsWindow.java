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
import com.googlecode.lanterna.gui.component.EditArea;
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
		super(guiScreen, "Products", true);
		
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
        		if(upcString == null || upcString.length() == 0)
        			return; // user canceled
        		ProductInfoWindow window = new ProductInfoWindow(guiScreen, Long.parseLong(upcString));
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Browse")) {
        		BrowseWindow window = new BrowseWindow(guiScreen, "Root");
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Compare Products")) {
        		Long upc1 = Long.parseLong((String)TextInputDialog.showTextInputBox(guiScreen, "Product 1", "Enter a upc", ""));
        		Long upc2 = Long.parseLong((String)TextInputDialog.showTextInputBox(guiScreen, "Product 2", "Enter a upc", ""));
        		CompareProductsWindow window = new CompareProductsWindow(guiScreen, upc1, upc2);
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	} else {
        		MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        	}
        }
	}
	
	public static class BrowseWindow extends MicrocenterWindow
	{
		Panel mainPanel;
		List<String> categories;
		List<String> products;
		Map<String, Long> productMap;
		public BrowseWindow(final GUIScreen guiScreen, String category_name)
		{
			super(guiScreen, "Browing " + category_name, true);
			
			mainPanel = new Panel();
			
			Connection dbConn = GlobalState.getDBConnection();
			try {
				PreparedStatement st = dbConn.prepareStatement(
						"SELECT hc.name " +
						"FROM product_type_tree AS hp " +
						"     JOIN product_type_tree AS hc " +
						"     ON hc.lft BETWEEN hp.lft and hp.rgt " +
						"WHERE hp.name = ? AND " +
						"      (SELECT COUNT(*) " +
						"       FROM product_type_tree hn " +
						"       WHERE hc.lft BETWEEN hn.lft and hn.rgt and hn.lft BETWEEN " +
						"             hp.lft AND hp.rgt) = " +
						"      1 + (SELECT COUNT(*) " +
						"           FROM product_type_tree hn " +
						"           WHERE hc.lft BETWEEN hn.lft and hn.rgt " +
						"           AND hn.lft BETWEEN hp.lft and hp.rgt and hn.name = ?);");
				st.setString(1, category_name);
				st.setString(2, category_name);
				ResultSet rs = st.executeQuery();
				
				categories = new ArrayList<String>();
				while(rs.next())
				{
					categories.add(rs.getString(1));
				}
				
				rs.close();
				st.close();
				
				// this is a leaf category
				if(categories.size() > 0)
				{
					ActionListBox categoryList = new ActionListBox();
					for(String category : categories)
					{
						categoryList.addAction(new CategoryListItem(category));
					}
					
					mainPanel.addComponent(categoryList);
				}
				else
				{
					products = new ArrayList<String>();
					productMap = new HashMap<String, Long>();
					st = dbConn.prepareStatement(
							"SELECT p.upc, substring(p.name for 27), b.name, substring(p.desc for 27) " +
		        			"FROM product as p, " +
		        			"     brand as b, " +
		        			"     product_type as pt, " +
		        			"     product_type_tree as ptt " +
		        			"WHERE p.brand = b.id AND " +
		        			"      p.upc = pt.upc AND " +
		        			"      pt.id = ptt.id AND " +
		        			"      ptt.name = ?;");
					st.setString(1, category_name);
					rs = st.executeQuery();
					
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
        				products.add(displayName.toString());
        				
        				productMap.put(displayName.toString(), rs.getLong(1));
					}
					
					ActionListBox productList = new ActionListBox();
					for(String product : products)
					{
						productList.addAction(new ProductListItem(product, productMap.get(product)));
					}
					mainPanel.addComponent(productList);
				}
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
				System.out.println(e.getMessage());
			}
			
			addComponent(mainPanel);
			
		}
		
		private class ProductListItem implements Action {
			private String label;
			private Long upc;
			
			public ProductListItem(String label, Long upc) 
			{
				this.label = label;
				this.upc = upc;
			}
			
			@Override
			public String toString() {
				return label;
			}
			
			@Override
			public void doAction()
			{
				ProductInfoWindow window = new ProductInfoWindow(guiScreen, upc);
				WindowManager.pushWindow(window);
				guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
			}
		}
		
		private class CategoryListItem implements Action {
			private String label;
			
			public CategoryListItem(String label)
			{
				this.label = label;
			}
			
			@Override
			public String toString() {
				return label;
			}
			
			@Override
			public void doAction() 
			{
				BrowseWindow window = new BrowseWindow(guiScreen, label);
				WindowManager.pushWindow(window);
				guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
			}
		}
	}
	
	public static class ProductInfoWindow extends MicrocenterWindow
	{
		private Long upc;
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
		private EditArea descEdit;
		private TextBox unitPriceBox;
		private Button brandButton;
		private Button storeButton;
		private Label stockLabel;
		public ProductInfoWindow(final GUIScreen guiScreen, final Long upc)
		{
			super(guiScreen, "Product Info", true);
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
			for(final String cat : categories)
			{
				categoryPanel.addComponent(new Button(cat, new Action() {
					@Override
					public void doAction() {
						close();
						BrowseWindow window = new BrowseWindow(guiScreen, cat);
						WindowManager.pushWindow(window);
						guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
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
						Connection dbConn = GlobalState.getDBConnection();
						try {
							Statement st = dbConn.createStatement();
							ResultSet rs = st.executeQuery(
									"SELECT b.name " +
									"FROM brand as b " +
									"ORDER BY b.name;");
							
							List<String> brands = new ArrayList<String>();
							while(rs.next())
							{
								brands.add(rs.getString(1));
							}
							
							String selectedBrand = (String)ListSelectDialog.showDialog(guiScreen, "Brands", "Select the new brand of the item", brands.toArray());
							if(selectedBrand == null)
								return;
							brandButton.setText(selectedBrand);
						} catch(SQLException e) {
							MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
							System.out.println(e.getMessage());
						}
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
			descEdit = new EditArea(guiScreen.getScreen().getTerminalSize(), newlineDesc.toString());
			descPanel.addComponent(descEdit);
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

			mainPanel.addComponent(new Button("View Specs", new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					try{
						PreparedStatement st = dbConn.prepareStatement(
								"SELECT ps.desc, ps.value " +
								"FROM product_spec as ps " +
								"WHERE ps.upc = ?;");
						st.setLong(1,upc);
						ResultSet rs = st.executeQuery();
						
						List<String> specs = new ArrayList<String>();
						while(rs.next())
						{
							specs.add(rs.getString(1) + " | " + rs.getString(2));
						}
						
						rs.close();
						st.close();
						
						if(specs.size() == 0)
						{
							MessageBox.showMessageBox(guiScreen, "Specs", "No specifications found for this product");
							return;
						}
						
						ListSelectDialog.showDialog(guiScreen, "Specifications", "", specs.toArray());
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
						System.out.println(e.getMessage());
					}
				}
			}));
			
			Panel stockPanel = new Panel(Panel.Orientation.HORISONTAL);
			storeButton = new Button(currStoreName, new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					try {
						Statement st = dbConn.createStatement();
						ResultSet rs = st.executeQuery(
								"SELECT s.id, s.name " +
								"FROM store as s " +
								"ORDER BY s.id;");
						
						List<String> stores = new ArrayList<String>();
						while(rs.next())
						{
							stores.add("["+rs.getInt(1)+"] "+rs.getString(2));
						}
						
						rs.close();
						st.close();
						
						if(stores.size() == 0)
						{
							MessageBox.showMessageBox(guiScreen, "Error", "No stores found");
							return;
						}
						
						String selectedStore = (String)ListSelectDialog.showDialog(guiScreen, "Stores", "List of available stores", stores.toArray());
						if(selectedStore == null)
							return; // canceled
						
						String[] splitString = selectedStore.split(" ");
						currStore = Integer.parseInt(splitString[0].substring(1,splitString[0].length()-1));
						currStoreName = splitString[1];
						storeButton.setText(currStoreName);
						
						// now update the stock
						PreparedStatement pst = dbConn.prepareStatement(
								"SELECT s.amount " +
								"FROM stock as s " +
								"WHERE s.store_id = ? AND " +
								"      s.upc = ?;");
						pst.setInt(1, currStore);
						pst.setLong(2, upc);
						rs = pst.executeQuery();
						if(!rs.next())
						{
							MessageBox.showMessageBox(guiScreen, "Error", "No stock entry found for product");
							return;
						}
						
						stock = rs.getInt(1);
						stockLabel.setText(""+stock);
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
						System.out.println(e.getMessage());
					}
				}
			});
			stockPanel.addComponent(storeButton);
			stockPanel.addComponent(new Label(" stock: "));
			stockLabel = new Label("" + stock);
			stockPanel.addComponent(stockLabel);
			mainPanel.addComponent(stockPanel);
			
			Panel buttonsPanel = new Panel(Panel.Orientation.HORISONTAL);
			if(GlobalState.getUserRole() == GlobalState.UserRole.ANONYMOUS || GlobalState.getUserRole() == GlobalState.UserRole.CUSTOMER)
			{
				Button addToCartButton = new Button("Add to Cart", new Action() {
					@Override
					public void doAction() {
						if(Integer.parseInt(stockLabel.getText()) == 0)
						{
							MessageBox.showMessageBox(guiScreen, "Error", "The currently selected store does not have any stock. Please try another store");
							return;
						}
						String quantity = (String)TextInputDialog.showTextInputBox(guiScreen, "Quantity", "Enter amount", "1");
						if(quantity == null || quantity.length() == 0)
							return; // user aborted
						if(Integer.parseInt(quantity) > Integer.parseInt(stockLabel.getText()) - GlobalState.getCartItemQuantity(upc, currStore))
						{
							MessageBox.showMessageBox(guiScreen, "Error", "Sorry there is not enough stock to order that amount at this time");
							return;
						}
						try {
							GlobalState.addCartItem(upc, currStore, currStoreName, name, Integer.parseInt(quantity), unitPrice);
						} catch (NumberFormatException e) {
							MessageBox.showMessageBox(guiScreen, "Input Error", e.getMessage());
							return;
						} catch (Exception e) {
							MessageBox.showMessageBox(guiScreen, "Error", e.getMessage());
						}
					}
				});
				buttonsPanel.addComponent(addToCartButton);
			}
			else
			{
				Button saveButton = new Button("Save Changes", new Action() {
					@Override
					public void doAction() {
						if(nameBox.getText().trim().length() == 0)
						{
							MessageBox.showMessageBox(guiScreen, "Error", "Product name cannot be blank");
							return;
						}
						if(descEdit.getData().trim().length() == 0)
						{
							MessageBox.showMessageBox(guiScreen, "Error", "Product description cannot be blank");
							return;
						}
						if(Double.parseDouble(unitPriceBox.getText()) < 0)
						{
							MessageBox.showMessageBox(guiScreen, "Error", "Price must be positive");
							return;
						}
						Connection dbConn = GlobalState.getDBConnection();
						try{
							PreparedStatement st = dbConn.prepareStatement(
									"UPDATE product " +
									"SET name=?,\"desc\"=?,unit_price=round((?)::numeric, 2), brand=(SELECT b.id " +
									"                                                            FROM brand as b " +
									"                                                            WHERE b.name = ? " +
									"                                                            LIMIT 1)" +
									"WHERE upc=?;");
							st.setString(1, nameBox.getText().trim());
							st.setString(2, descEdit.getData().trim());
							st.setDouble(3, Double.parseDouble(unitPriceBox.getText()));
							st.setString(4, brandButton.getText());
							st.setLong(5, upc);
							st.executeUpdate();
							st.close();
							
							MessageBox.showMessageBox(guiScreen, "Success", "Successfully saved changes");
						} catch(SQLException e) {
							MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
							System.out.println(e.getMessage());
						}
					}
				});
				buttonsPanel.addComponent(saveButton);
			}
			mainPanel.addComponent(buttonsPanel);
			
			addComponent(mainPanel);
		}
		
	}

	private class CompareProductsWindow extends MicrocenterWindow
	{
		Panel mainPanel;
		List<String> specDesc;
		List<String> p1Specs;
		List<String> p2Specs;
		public CompareProductsWindow(final GUIScreen guiScreen, Long upc1, Long upc2)
		{
			super(guiScreen, "Comparing " + upc1 + " and " + upc2, true);
			
			mainPanel = new Panel();
			
			Connection dbConn = GlobalState.getDBConnection();
			try{
				PreparedStatement st = dbConn.prepareStatement(
						"SELECT p1.desc, p1.value, p2.desc, p2.value " +
						"FROM (SELECT * " +
						"      FROM product_spec " +
						"      WHERE upc=?) as p1 " +
						"     FULL OUTER JOIN " +
						"    (SELECT * " +
						"     FROM product_spec " +
						"     WHERE upc=?) as p2 " +
						"    ON p1.desc = p2.desc;");
				st.setLong(1, upc1);
				st.setLong(2, upc2);
				ResultSet rs = st.executeQuery();
				
				specDesc = new ArrayList<String>();
				p1Specs = new ArrayList<String>();
				p2Specs = new ArrayList<String>();
				
				while(rs.next())
				{
					String desc1 = rs.getString(1);
					String desc2 = rs.getString(3);

					if(desc1 == null)
					{
						specDesc.add(desc2+": ");
					}
					else
					{
						specDesc.add(desc1+": ");
					}
					
					String val = rs.getString(2);
					
					if(val == null)
					{
						p1Specs.add(" - ");
					}
					else
					{
						p1Specs.add(val);
					}
					
					val = rs.getString(4);
					if(val == null)
					{
						p2Specs.add(" - ");
					}
					else
					{
						p2Specs.add(val);
					}
				}
				
				StringBuilder comparison = new StringBuilder();
				comparison.append("Description | " + upc1 + " Specs | " + upc2 + " Specs\n");
				comparison.append("------------------------------------------------------\n");
				
				for(int i=0; i<specDesc.size(); i++)
				{
					comparison.append(specDesc.get(i));
					comparison.append(p1Specs.get(i));
					comparison.append(" | ");
					comparison.append(p2Specs.get(i));
					comparison.append("\n");
				}
				
				TextArea comparisonArea = new TextArea(comparison.toString());
				mainPanel.addComponent(comparisonArea);
				
			} catch(SQLException e) {
				MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
				System.out.println(e.getMessage());
				close();
				return;
			}
			
			addComponent(mainPanel);
		}
	}
}
