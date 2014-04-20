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
	
	public MicrocenterWindow(final GUIScreen guiScreen, String label, boolean back, boolean checkout) {
		super(label);
		
		this.back = back;
		this.checkout = checkout;
		this.guiScreen = guiScreen;
		
		addedComponents = new ArrayList<AddedComponent>();
		
		mainPanel = new Panel();
		
		menuPanel = new MenuPanel(back, checkout);
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
        		MessageBox.showMessageBox(guiScreen, "Checkout", "Not yet implemented");
        	}
        });
        
        mainPanel.addShortcut('l', true, false, new Action() {
        	@Override
        	public void doAction() {
        		String username = TextInputDialog.showTextInputBox(guiScreen, "Username", "Enter your username", "");
        		String password = TextInputDialog.showPasswordInputBox(guiScreen, "Password", "Enter your password", "");
        		
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
        				
        				String hashedPassword = MicrocenterWindow.get_SHA_512_SecurePassword(password, salt);
        				
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
        		while(true)
        		{
	        		RegistrationWindow regWindow = new RegistrationWindow(guiScreen);
	        		guiScreen.showWindow(regWindow);
	        		
	        		if(regWindow.username == null)
	        		{
	        			// user canceled
	        			break;
	        		}
	        		
	        		if(!regWindow.password.equals(regWindow.passwordConfirm))
	        		{
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "Passwords did not match");
	        			continue;
	        		}
	        		
	        		Connection dbConnection = GlobalState.getDBConnection();
	        		try {
	        			PreparedStatement st = dbConnection.prepareStatement("SELECT username FROM users WHERE username=?;");
	        			st.setString(1, regWindow.username);
	        			ResultSet rs = st.executeQuery();
	        			
	        			// make sure username not already taken
	        			if(rs.next())
	        			{
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Username already taken");
	        				continue;
	        			}
	        			
	        			rs.close();
	        			st.close();
	        			
	        			st = dbConnection.prepareStatement("INSERT INTO customer(first_name) VALUES (?);",
	        					new String[] { "loyalty_number"} );
	        			st.setNull(1, java.sql.Types.VARCHAR);
	        			if(st.executeUpdate() <= 0)
	        			{
	        				st.close();
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Failed to create customer");
	        				break;
	        			}
	        			rs = st.getGeneratedKeys();
	        			Integer loyalty_number = null;
	        			if(!rs.next())
	        			{
	        				rs.close();
	        				st.close();
	        				MessageBox.showMessageBox(guiScreen, "Registration Error", "Failed to create customer");
	        				break;
	        			}
	        			loyalty_number = rs.getInt(1);
	        			rs.close();
	        			st.close();
	        			
	        			String salt = MicrocenterWindow.getSalt();
	        			String hashedPassword = get_SHA_512_SecurePassword(regWindow.password, salt);
	        			st = dbConnection.prepareStatement("INSERT INTO users (username, password, salt, role, loyalty_number) VALUES (?, ?, ?, 'customer', ?);");
	        			st.setString(1, regWindow.username);
	        			st.setString(2, hashedPassword);
	        			st.setString(3, salt);
	        			st.setInt(4, loyalty_number);
	        			st.executeUpdate();
	        			MessageBox.showMessageBox(guiScreen, "Registration Success", "Registered and logged in");
	        			
	        			GlobalState.setUserRole(GlobalState.UserRole.CUSTOMER);
	        			GlobalState.setCustomerNumber(loyalty_number);
	            		WindowManager.refreshWindow();
	        			break;
	        		} catch(SQLException e) {
	        			MessageBox.showMessageBox(guiScreen, "Registration Error", "SQL Error: " + e.getMessage());
	        			break;
	        		} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
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
		menuPanel = new MenuPanel(back, checkout);
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
	
	//Add salt
    private static String getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }
    
    private static String get_SHA_512_SecurePassword(String password, String salt)
    {
        MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");

	        md.update(salt.getBytes());
	        byte[] bytes = md.digest(password.getBytes());
	        StringBuilder sb = new StringBuilder();
	        for(int i=0; i< bytes.length ;i++)
	        {
	            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        //Get complete hashed password in hex format
	        return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
    }
    
    private class RegistrationWindow extends Window {

    	private final GUIScreen guiScreen;
    	public String username = null;
    	public String password = null;
    	public String passwordConfirm = null;
    	
    	private Panel mainPanel;
    	private TextBox usernameBox;
    	private TextBox passwordBox;
    	private TextBox passwordConfirmBox;
    	
		public RegistrationWindow(GUIScreen guiScreen) {
			super("Register");
			this.guiScreen = guiScreen;
			
			mainPanel = new Panel();
			mainPanel.addComponent(new Label("Username: "));
			usernameBox = new TextBox("");
			mainPanel.addComponent(usernameBox);
			mainPanel.addComponent(new Label("Password: "));
			passwordBox = new PasswordBox();
			mainPanel.addComponent(passwordBox);
			mainPanel.addComponent(new Label("Confirm Password: "));
			passwordConfirmBox = new PasswordBox();
			mainPanel.addComponent(passwordConfirmBox);
			
			Panel buttonPanel = new Panel(Panel.Orientation.HORISONTAL);
			buttonPanel.addComponent(new Button("Ok", new Action() {
				@Override
				public void doAction() {
					username = usernameBox.getText();
					password = passwordBox.getText();
					passwordConfirm = passwordConfirmBox.getText();
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
			addComponent(mainPanel);
		}
    	
    }
}
