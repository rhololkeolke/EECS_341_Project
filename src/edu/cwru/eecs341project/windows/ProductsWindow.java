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
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;

import edu.cwru.eecs341project.GlobalState;

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
        			System.out.println(searchResultsMap.get(selectedProduct));
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
        			System.out.println(e.getMessage());
        		}
        	} else {
        		MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        	}
        }
	}

}
