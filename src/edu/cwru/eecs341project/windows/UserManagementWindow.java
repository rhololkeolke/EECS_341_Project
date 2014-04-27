package edu.cwru.eecs341project.windows;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.PasswordBox;
import com.googlecode.lanterna.gui.component.RadioCheckBoxList;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;

import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

public class UserManagementWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;
	private final GUIScreen guiScreen;

	public UserManagementWindow(final GUIScreen guiScreen, String label, boolean checkout) {
		super(guiScreen, label, true, checkout);
		this.guiScreen = guiScreen;
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Create User"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "List Users"));
		actionListBox.addAction(new ActionListBoxItem(guiScreen, "Delete User"));
		actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
	}
	
	private class ActionListBoxItem implements Action {
        private String label;

        public ActionListBoxItem(GUIScreen owner, String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }

        public void doAction() {
        	if(label.equals("List Users"))
        	{
        		String user = null;
        		Connection dbConn = GlobalState.getDBConnection();
        		List<String> listOptions = new ArrayList<String>();
        		try {
        			Statement st = dbConn.createStatement();
        			ResultSet rs = st.executeQuery("SELECT username, role FROM users ORDER BY username;");
        			while(rs.next())
        			{
        				listOptions.add(rs.getString(1) + " (" + rs.getString(2) + ")");
        			}
        			
        			user = (String)ListSelectDialog.showDialog(guiScreen, "Users List", "All users in database", listOptions.toArray());
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "SQL Error", "Error getting list of users" + e.getMessage());
        			return;
        		}
        		if(user == null)
        			return;
        		String[] splitStrings = user.split(" ");
        		String username = splitStrings[0];
        		String role = splitStrings[1].substring(1, splitStrings[1].length()-1);
        		UserInfoWindow window = new UserInfoWindow(guiScreen, username, role);
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
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
        		MessageBox.showMessageBox(guiScreen, "Action", "Selected " + label);
        	}
        }
	}
	
	private class UserInfoWindow extends MicrocenterWindow {
		private Panel mainPanel;
		final String username;
		TextBox usernameBox = null;
		RadioCheckBoxList accountTypeRadio = null;
		PasswordBox passwordBox = null;
		PasswordBox confirmPasswordBox = null;
		
		public UserInfoWindow(final GUIScreen guiScreen, final String username, String accountType)
		{
			super(guiScreen, "User Info", true, false);
			mainPanel = new Panel();
			
			this.username = username;
			
			Panel usernamePanel = new Panel(Panel.Orientation.HORISONTAL);
			usernamePanel.addComponent(new Label("Username: "));
			usernameBox = new TextBox(username);
			usernamePanel.addComponent(usernameBox);
			mainPanel.addComponent(usernamePanel);
			
			Panel accountTypePanel = new Panel(Panel.Orientation.HORISONTAL);
			accountTypePanel.addComponent(new Label("Account Type: "));
			accountTypeRadio = new RadioCheckBoxList();
			accountTypeRadio.addItem("Customer");
			accountTypeRadio.addItem("Employee");
			accountTypeRadio.addItem("DBA");
			
			if(accountType.equals("customer"))
				accountTypeRadio.setCheckedItemIndex(0);
			else if(accountType.equals("employee"))
				accountTypeRadio.setCheckedItemIndex(1);
			else if(accountType.equals("DBA"))
				accountTypeRadio.setCheckedItemIndex(2);
			else {
				accountTypeRadio.addItem(accountType);
				accountTypeRadio.setCheckedItemIndex(3);
			}
			accountTypePanel.addComponent(accountTypeRadio);
			mainPanel.addComponent(accountTypePanel);
			
			Panel passwordPanel = new Panel(Panel.Orientation.HORISONTAL);
			passwordPanel.addComponent(new Label("Password: "));
			passwordBox = new PasswordBox();
			passwordPanel.addComponent(passwordBox);
			mainPanel.addComponent(passwordPanel);
			
			Panel confirmPasswordPanel = new Panel(Panel.Orientation.HORISONTAL);
			confirmPasswordPanel.addComponent(new Label("Confirm Password: "));
			confirmPasswordBox = new PasswordBox();
			confirmPasswordPanel.addComponent(confirmPasswordBox);
			mainPanel.addComponent(confirmPasswordPanel);
			
			Panel buttonsPanel = new Panel(Panel.Orientation.HORISONTAL);
			buttonsPanel.addComponent(new Button("Save Changes", new Action() {
				@Override
				public void doAction() {
					Connection dbConn = GlobalState.getDBConnection();
					try{
						PreparedStatement st;
						if(passwordBox.getText().length() > 0)
						{
							if(!passwordBox.getText().equals(confirmPasswordBox.getText()))
							{
								MessageBox.showMessageBox(guiScreen, "ERROR", "Passwords do not match");
								return;
							}
							if(usernameBox.getText().length() <= 0)
							{
								MessageBox.showMessageBox(guiScreen, "Error", "Username cannot be blank");
								return;
							}
							if(usernameBox.getText().equals(username))
							{
								st = dbConn.prepareStatement("SELECT * FROM users WHERE username=?;");
								st.setString(1, usernameBox.getText());
								ResultSet rs = st.executeQuery();
								rs.next();
								if(rs.next())
								{
									MessageBox.showMessageBox(guiScreen, "Error", "Username " + usernameBox.getText() + " already exists");
									return;
								}
								rs.close();
								st.close();
							}
							

							st = dbConn.prepareStatement("UPDATE users "
									+ "SET role=?, username=?, password=?, salt=? "
									+ "WHERE username=?;");
							String salt = GlobalState.getSalt();
							String hashedPassword = GlobalState.get_SHA_512_SecurePassword(passwordBox.getText(), salt);

							st.setString(3, hashedPassword);
							st.setString(4, salt);
							st.setString(5, username);
							
						} else {
							st = dbConn.prepareStatement("UPDATE users "
									+ "SET role=?, username=? "
									+ "WHERE username=?;");
							st.setString(3,  username);
						}
						st.setString(1, ((String)accountTypeRadio.getItemAt(accountTypeRadio.getSelectedIndex())).toLowerCase());
						st.setString(2, usernameBox.getText());
						st.executeUpdate();
						st.close();
						close();
					
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
						System.out.println(e.getMessage());
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}));
			buttonsPanel.addComponent(new Button("Delete User", new Action() {
				@Override
				public void doAction() {
					DialogResult result = MessageBox.showMessageBox(guiScreen, "Delete user", "Are you sure you want to delete " + username + "?", DialogButtons.YES_NO);
					if(result.equals(DialogResult.NO))
					{
						return;
					}
					Connection dbConn = GlobalState.getDBConnection();
					try {
						PreparedStatement st = dbConn.prepareStatement("DELETE FROM users WHERE username=?;");
						st.setString(1, username);
						st.executeUpdate();
						close();
					} catch(SQLException e) {
						MessageBox.showMessageBox(guiScreen, "SQL Error", e.getMessage());
					}
				}
			}));
			mainPanel.addComponent(buttonsPanel);
			
			addComponent(mainPanel);
		}	
	}

}
