package edu.cwru.eecs341project.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;

import edu.cwru.eecs341project.GlobalState;

public class UserManagementWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;

	public UserManagementWindow(GUIScreen guiScreen, String label, boolean checkout) {
		super(guiScreen, label, true, checkout);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Create User"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "List Users"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Delete User"));
		actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
	}
	
	private class ActionListBoxItem implements Action {
        private GUIScreen owner;
        private String label;

        public ActionListBoxItem(GUIScreen owner, String label) {
            this.label = label;
            this.owner = owner;
        }
        
        @Override
        public String toString() {
            return label;
        }

        public void doAction() {
        	if(label.equals("List Users"))
        	{
        		Connection dbConn = GlobalState.getDBConnection();
        		List<String> listOptions = new ArrayList<String>();
        		try {
        			Statement st = dbConn.createStatement();
        			ResultSet rs = st.executeQuery("SELECT username, role FROM users ORDER BY username;");
        			while(rs.next())
        			{
        				listOptions.add(rs.getString(1) + " (" + rs.getString(2) + ")");
        			}
        			
        			ListSelectDialog.showDialog(guiScreen, "Users List", "All users in database", listOptions.toArray());
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", "Error getting list of users" + e.getMessage());
        			return;
        		}
        	} else if(label.equals("Delete User")) {
        		Connection dbConn = GlobalState.getDBConnection();
        		String username = TextInputDialog.showTextInputBox(guiScreen, "Delete User", "Enter username to delete", "");
        		
        		if(username == null)
        			return; // user canceled
        		
        		if(username.length() == 0)
        		{
        			MessageBox.showMessageBox(guiScreen, "Deletion Error", "Username cannot be blank");
        			return;
        		}
        		
        		if(username.equals(GlobalState.getUsername()))
        		{
        			MessageBox.showMessageBox(guiScreen, "Deletion Error", "Cannot delete current user");
        			return;
        		}
        		
        		try {
        			// get info about the username
        			PreparedStatement st = dbConn.prepareStatement("SELECT role FROM users WHERE username = ?;");
        			st.setString(1, username);
        			ResultSet rs = st.executeQuery();
        			if(!rs.next())
        			{
        				MessageBox.showMessageBox(guiScreen, "Deletion Error", "Username " + username + " does not exist");
        				return;
        			}

        			String userRole = rs.getString(1);
        			rs.close();
        			st.close();
        			
        			if(userRole != null && userRole.equals("DBA"))
        			{
        				Statement dbaCount = dbConn.createStatement();
        				rs = dbaCount.executeQuery("SELECT COUNT(*) FROM users WHERE role='DBA';");
        				
        				rs.next();
        				
        				int count = rs.getInt(1);
        				rs.close();
        				dbaCount.close();
        				if(count <= 1)
        				{
        					MessageBox.showMessageBox(guiScreen, "Deletion Error", "Cannot delete last DBA");
        					return;
        				}
        			}
        			
        			st = dbConn.prepareStatement("DELETE FROM users WHERE username = ?;");
        			st.setString(1, username);
        			st.executeUpdate();
        			st.close();
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
        		}
        		
        		MessageBox.showMessageBox(guiScreen, "Deletion", "Successfully deleted " + username);
        	}
        	else {
        		MessageBox.showMessageBox(owner, "Action", "Selected " + label);
        	}
        }
	}

}
