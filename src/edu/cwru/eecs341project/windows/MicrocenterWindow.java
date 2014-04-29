package edu.cwru.eecs341project.windows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.PasswordBox;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;
import com.googlecode.lanterna.gui.layout.LayoutParameter;
import com.googlecode.lanterna.input.Key;

import edu.cwru.eecs341project.AddedComponent;
import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

public class MicrocenterWindow extends Window implements ManagedWindow{
	private Panel mainPanel;
	private MenuPanel menuPanel;
	private boolean back, checkout;
	protected final GUIScreen guiScreen;
	
	private List<AddedComponent> addedComponents;
	
	public MicrocenterWindow(final GUIScreen guiScreen, String label, boolean back) {
		super(label);
		
		this.back = back;
		this.checkout = checkout;
		this.guiScreen = guiScreen;
		
		addedComponents = new ArrayList<AddedComponent>();
		
		mainPanel = new Panel();
		
		menuPanel = new MenuPanel(back);
		mainPanel.addComponent(menuPanel);
		
		mainPanel.addComponent(new Label(""));
		mainPanel.addComponent(new Label(""));
				
		mainPanel.addShortcut(Key.Kind.Escape, new Action() {
			@Override
			public void doAction() {
				MicrocenterWindow.this.close();
			}
		});
        mainPanel.addShortcut('c', true, false, new Action() {
        	@Override
        	public void doAction() {
        		if(GlobalState.cartIsEmpty())
        		{
        			MessageBox.showMessageBox(guiScreen, "Cart", "Your shopping cart is empty. Please add products and then try again");
        			return;
        		}
        		CheckoutWindow window = new CheckoutWindow(guiScreen);
        		WindowManager.pushWindow(window);
        		guiScreen.showWindow(window, GUIScreen.Position.FULL_SCREEN);
        	}
        });
        
        mainPanel.addShortcut('l', true, false, new Action() {
        	@Override
        	public void doAction() {
        		
        		// if logged in then logout
        		if(GlobalState.getUserRole() != GlobalState.UserRole.ANONYMOUS)
        		{
        			GlobalState.setUserRole(GlobalState.UserRole.ANONYMOUS);
        			WindowManager.exitToMain();
        			WindowManager.refreshWindow();
        			return;
        		}
        		
        		// otherwise login
        		String username = TextInputDialog.showTextInputBox(guiScreen, "Username", "Enter your username", "");
        		if(username == null)
        			return; // user canceled
        		
        		String password = TextInputDialog.showPasswordInputBox(guiScreen, "Password", "Enter your password", "");
        		if(password == null)
        			return; // user canceled
        		
        		Connection dbConnection = GlobalState.getDBConnection();
        		try {
        			PreparedStatement st = dbConnection.prepareStatement("SELECT password, salt, role, loyalty_number FROM users WHERE username=?;");
        			st.setString(1, username);
        			ResultSet queryResult = st.executeQuery();
        			
        			if(queryResult.next())
        			{
        				// check the supplied password
        				String salt = queryResult.getString(2);
        				String storedPassword = queryResult.getString(1);
        				
        				String hashedPassword = GlobalState.get_SHA_512_SecurePassword(password, salt);
        				
        				if(storedPassword.equals(hashedPassword))
        				{
        					String role = queryResult.getString(3);
        					if(role.equals("customer"))
        					{
        						GlobalState.setUserRole(GlobalState.UserRole.CUSTOMER);
        						GlobalState.setCustomerNumber(Integer.parseInt(queryResult.getString(4)));
        					} else if(role.equals("employee")) {
        						GlobalState.setUserRole(GlobalState.UserRole.EMPLOYEE);
        					} else if(role.equals("DBA")) {
        						GlobalState.setUserRole(GlobalState.UserRole.DBA);
        					} else {
        						MessageBox.showMessageBox(guiScreen, "Login Error", "Bad role. See DBA");
        					}
        				}
        			}
        			else
        			{
        				MessageBox.showMessageBox(guiScreen, "Login Error", "Incorrect username/password");
        			}
        			GlobalState.setUsername(username);
        			queryResult.close();
        			st.close();
        		} catch(SQLException e) {
        			MessageBox.showMessageBox(guiScreen, "Login Error", "SQL Error " + e.getMessage());
        		}
        		
        		WindowManager.refreshWindow();
        	}
        });
        
        mainPanel.addShortcut('r', true, false, new Action() {
        	@Override
        	public void doAction() {
        		// only allow registration if not already logged in
        		if(GlobalState.getUserRole() != GlobalState.UserRole.ANONYMOUS)
        			return;
        		while(true)
        		{
	        		RegistrationWindow regWindow = new RegistrationWindow(guiScreen);
	        		guiScreen.showWindow(regWindow, GUIScreen.Position.CENTER);
	        		
	        		if(regWindow.firstName == null)
	        		{
	        			// user canceled
	        			break;
	        		}
	        		
	        		// check input validity
	        		if(regWindow.firstName.length() == 0)
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Must provide a first name");
	        			continue;
	        		}
	        		if(regWindow.lastName.length() == 0)
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Must provide a last name");
	        			continue;
	        		}
	        		if(regWindow.username.length() == 0)
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Must provide a username");
	        			continue;
	        		}
	        		if(regWindow.password.length() == 0)
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Password cannot be blank");
	        			continue;
	        		}
	        		if(regWindow.phoneNumber.length() <= 0 || regWindow.phoneNumber.length() > 13)
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Invalid phone number");
	        			continue;
	        		}
	        		
	        		if(!regWindow.password.equals(regWindow.passwordConfirm))
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Passwords did not match");
	        			continue;
	        		}
	        		
	        		// now do the database work with the provided info
	        		Connection dbConnection = GlobalState.getDBConnection();
	        		try {
	        			PreparedStatement st = dbConnection.prepareStatement("SELECT c.loyalty_number FROM customer as c, customer_phone as p WHERE c.loyalty_number = p.loyalty_number AND c.first_name=? AND c.last_name=? AND p.phone = ?;");
	        			st.setString(1, regWindow.firstName);
	        			st.setString(2, regWindow.lastName);
	        			st.setString(3, regWindow.phoneNumber);
	        			ResultSet rs = st.executeQuery();
	        			
	        			// make sure customer doesn't already exist
	        			if(rs.next())
	        			{
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Customer already exists");
	        				continue;
	        			}
	        			
	        			rs.close();
	        			st.close();
	        			
	        			st = dbConnection.prepareStatement("SELECT * FROM users WHERE username=?;");
	        			st.setString(1, regWindow.username);
	        			rs = st.executeQuery();
	        			
	        			if(rs.next())
	        			{
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Username taken");
	        				continue;
	        			}
	        			
	        			rs.close();
	        			st.close();
	        			
	        			dbConnection.setAutoCommit(false);
	        			st = dbConnection.prepareStatement("INSERT INTO customer(first_name, last_name) VALUES (?, ?);",
	        					new String[] { "loyalty_number"} );
	        			st.setString(1, regWindow.firstName);
	        			st.setString(2, regWindow.lastName);
	        			if(st.executeUpdate() <= 0)
	        			{
	        				dbConnection.rollback();
	        				st.close();
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Failed to create customer");
	        				break;
	        			}
	        			rs = st.getGeneratedKeys();
	        			Integer loyaltyNumber = null;
	        			if(!rs.next())
	        			{
	        				dbConnection.rollback();
	        				rs.close();
	        				st.close();
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Failed to create customer");
	        				break;
	        			}
	        			loyaltyNumber = rs.getInt(1);
	        			rs.close();
	        			st.close();
	        			
	        			st = dbConnection.prepareStatement("INSERT INTO customer_phone(loyalty_number, phone) VALUES (?, ?);");
	        			st.setInt(1, loyaltyNumber);
	        			st.setString(2, regWindow.phoneNumber);
	        			if(st.executeUpdate() <= 0)
	        			{
	        				dbConnection.rollback();
	        				st.close();
	        				MessageBox.showMessageBox(guiScreen, "RegistrationError", "Failed to create customer");
	        				break;
	        			}
	        			
	        			String salt = GlobalState.getSalt();
	        			String hashedPassword = GlobalState.get_SHA_512_SecurePassword(regWindow.password, salt);
	        			st = dbConnection.prepareStatement("INSERT INTO users (username, password, salt, role, loyalty_number) VALUES (?, ?, ?, 'customer', ?);");
	        			st.setString(1, regWindow.username);
	        			st.setString(2, hashedPassword);
	        			st.setString(3, salt);
	        			st.setInt(4, loyaltyNumber);
	        			if(st.executeUpdate() <= 0)
	        			{
	        				dbConnection.rollback();
	        				st.close();
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Failed to create customer");
	        				break;
	        			}
	        			dbConnection.commit();
	        			MessageBox.showMessageBox(guiScreen, "Registration Success", "Registered and logged in");
	        			
	        			GlobalState.setUserRole(GlobalState.UserRole.CUSTOMER);
	        			GlobalState.setCustomerNumber(loyaltyNumber);
	            		WindowManager.refreshWindow();
	        			break;
	        		} catch(SQLException e) {
	        			
	        			try {
	        				if(!dbConnection.getAutoCommit())
	        					dbConnection.rollback();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
	        			
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "SQL Error: " + e.getMessage());
	        			break;
	        		} catch (NoSuchAlgorithmException e) {
	        			try {
	        				if(!dbConnection.getAutoCommit())
	        					dbConnection.rollback();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					} finally {
						try {
							dbConnection.setAutoCommit(true);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
        		}
        		
        	}
        });
        		
		super.addComponent(mainPanel);
		WindowManager.pushWindow(this);
	}
	
	@Override
	public void addComponent(Component component, LayoutParameter... layoutParameters)
    {
		super.removeComponent(mainPanel);
		addedComponents.add(new AddedComponent(component, layoutParameters));
		mainPanel.addComponent(component, layoutParameters);
		super.addComponent(mainPanel);
    }
	
	@Override
	public void refresh(){
		mainPanel.removeAllComponents();
		menuPanel = new MenuPanel(back);
		mainPanel.addComponent(menuPanel);
		mainPanel.addComponent(new Label(""));
		mainPanel.addComponent(new Label(""));
		for(AddedComponent addedComponent : addedComponents)
		{
			mainPanel.addComponent(addedComponent.component, addedComponent.layoutParameters);
		}
	}
	
	@Override
	public void removeComponent(Component component)
	{
		mainPanel.removeComponent(component);
		// loop until found in the list and then remove
		for(int i=0; i<addedComponents.size(); i++)
		{
			if(addedComponents.get(i).equals(component))
			{
				addedComponents.remove(i);
				break;
			}
		}
	}
	
	@Override
	public void removeAllComponents()
	{
		addedComponents = new ArrayList<AddedComponent>();
		refresh();
	}
	
	@Override
	public void close()
	{
		WindowManager.popWindow();
		super.close();
	}
	
	
    
    private class RegistrationWindow extends Window {

    	private final GUIScreen guiScreen;
    	public String firstName = null;
    	public String lastName = null;
    	public String username = null;
    	public String password = null;
    	public String passwordConfirm = null;
    	public String phoneNumber = null;
    	
    	private Panel mainPanel;
    	private TextBox firstNameBox;
    	private TextBox lastNameBox;
    	private TextBox usernameBox;
    	private TextBox passwordBox;
    	private TextBox passwordConfirmBox;
    	private TextBox areaCodeBox;
    	private TextBox firstDigitsBox;
    	private TextBox lastDigitsBox;
    	
		public RegistrationWindow(GUIScreen guiScreen) {
			super("Register");
			this.guiScreen = guiScreen;
			
			mainPanel = new Panel();
			mainPanel.addComponent(new Label("First Name"));
			firstNameBox = new TextBox("");
			mainPanel.addComponent(firstNameBox);
			mainPanel.addComponent(new Label("Last Name"));
			lastNameBox = new TextBox("");
			mainPanel.addComponent(lastNameBox);
			mainPanel.addComponent(new Label("Username"));
			usernameBox = new TextBox("");
			mainPanel.addComponent(usernameBox);
			mainPanel.addComponent(new Label("Password"));
			passwordBox = new PasswordBox();
			mainPanel.addComponent(passwordBox);
			mainPanel.addComponent(new Label("Confirm Password"));
			passwordConfirmBox = new PasswordBox();
			mainPanel.addComponent(passwordConfirmBox);
			mainPanel.addComponent(new Label("Phone Number"));
			
			Panel phonePanel = new Panel(Panel.Orientation.HORISONTAL);
			areaCodeBox = new TextBox("");
			phonePanel.addComponent(areaCodeBox);
			firstDigitsBox = new TextBox("");
			phonePanel.addComponent(firstDigitsBox);
			lastDigitsBox = new TextBox("");
			phonePanel.addComponent(lastDigitsBox);
			mainPanel.addComponent(phonePanel);
			
			Panel buttonPanel = new Panel(Panel.Orientation.HORISONTAL);
			buttonPanel.addComponent(new Button("Ok", new Action() {
				@Override
				public void doAction() {
					firstName = firstNameBox.getText().trim();
					lastName = lastNameBox.getText().trim();
					username = usernameBox.getText().trim();
					password = passwordBox.getText().trim();
					passwordConfirm = passwordConfirmBox.getText().trim();
					StringBuilder sb = new StringBuilder();
					sb.append("(");
					sb.append(areaCodeBox.getText());
					sb.append(")");
					sb.append(firstDigitsBox.getText());
					sb.append("-");
					sb.append(lastDigitsBox.getText());
					phoneNumber = sb.toString();
					close();
				}
			}));
			buttonPanel.addComponent(new Button("Cancel", new Action() {
				@Override
				public void doAction() {
					close();
				}
			}));
			mainPanel.addComponent(buttonPanel);
			
			mainPanel.addShortcut(Key.Kind.Escape, new Action() {
				@Override
				public void doAction() {
					RegistrationWindow.this.close();
				}
			});
			addComponent(mainPanel);
		}
    	
    }
}
