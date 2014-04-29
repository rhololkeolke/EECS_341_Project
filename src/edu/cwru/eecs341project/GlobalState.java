package edu.cwru.eecs341project;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class GlobalState {
	
	public enum UserRole {
		ANONYMOUS, CUSTOMER, EMPLOYEE, DBA
	}
	
	public static class MixedStoreOrderException extends Exception {
		public MixedStoreOrderException(String message)
		{
			super(message);
		}
	}
	
	private static int customerNumber = -1;
	private static UserRole userRole = UserRole.ANONYMOUS;
	private static Connection dbConnection = null;
	private static String username = null;
	private static ShoppingCart cart = new ShoppingCart();
	public static final int anonymousCustNum = 3010;
	
	public static boolean cartIsEmpty() {
		return (cart.getItems().size() == 0);
	}
	
	public static void clearCart() {
		cart.clear();
	}
	
	public static List<CartItem> getCartItems() {
		return cart.getItems();
	}
	
	public static void addCartItem(Long upc, int storeId, String storeName, String name, int quantity, double price) throws Exception, MixedStoreOrderException {
		List<CartItem> items = cart.getItems();
		for(CartItem item : items)
		{
			if(item.storeId != storeId)
				throw new MixedStoreOrderException("Error: Cannot order from multiple stores in a single order");
		}
		
		cart.addItem(new CartItem(upc, storeId, storeName, name, quantity, price));
		WindowManager.refreshAllWindows();
	}
	
	public static void addCartItem(CartItem i) throws Exception, MixedStoreOrderException
	{
		List<CartItem> items = cart.getItems();
		for(CartItem item : items)
		{
			if(item.storeId != i.storeId)
				throw new MixedStoreOrderException("Error: Cannot order from multiple stores in a single order");
		}
		cart.addItem(i);
		WindowManager.refreshAllWindows();
	}
	
	public static void removeCartItem(CartItem item) {
		cart.removeItem(item);
		WindowManager.refreshAllWindows();
	}
	
	public static void updateCartItem(CartItem item) throws Exception, MixedStoreOrderException {
		List<CartItem> items = cart.getItems();
		for(CartItem existingItem : items)
		{
			if(item.storeId != existingItem.storeId)
				throw new MixedStoreOrderException("Error: Cannot order from multiple stores in a single order");
		}
		cart.addItem(item);
		WindowManager.refreshAllWindows();
	}
	
	public static int getCartItemQuantity(Long upc, int storeId) {
		CartItem item = cart.getItem(upc, storeId);
		if(item == null)
			return 0;
		return item.getQuantity();
	}
	
	public static int getCustomerNumber() throws Exception {
		if(userRole == UserRole.ANONYMOUS || userRole == UserRole.EMPLOYEE || userRole == UserRole.DBA)
		{
			throw new Exception("current user is not a customer");
		}
		return customerNumber;
	}
	
	public static void setCustomerNumber(int custNum) {
		customerNumber = custNum;
	}
	
	public static String getUsername() {
		if(userRole == UserRole.ANONYMOUS)
			return null;
		return username;
	}
	
	public static void setUsername(String username) {
		GlobalState.username = username; 
	}
	
	public static UserRole getUserRole()
	{
		return userRole;
	}
	
	public static void setUserRole(UserRole role) {
		userRole = role;
	}
	
	public static Connection getDBConnection() {
		if(dbConnection == null)
		{
			Document doc = null;
			try {
				File fXmlFile = new File("db_settings.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(fXmlFile);
			} catch(Exception e) {
				System.out.println("Error opening database settings file");
				e.printStackTrace();
			}
			if(doc == null)
			{
				System.out.println("Error opening database config file");
				return dbConnection;
			}
			String user = doc.getElementsByTagName("user").item(0).getTextContent();
			String password = doc.getElementsByTagName("password").item(0).getTextContent();
			String host = doc.getElementsByTagName("host").item(0).getTextContent();
			String port = doc.getElementsByTagName("port").item(0).getTextContent();
			String dbName = doc.getElementsByTagName("dbName").item(0).getTextContent();
			
			String dbURL = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?user=" + user + "&" + "password=" + password;
			//System.out.println(dbURL);
			
			try {
				Class.forName("org.postgresql.Driver");
			} catch(ClassNotFoundException e) {
				System.out.println("Error opening driver class");
				e.printStackTrace();
			}
			
			try {
				
				dbConnection = DriverManager.getConnection(dbURL);
			} catch(SQLException e) {
				System.out.println("Error: could not connect to database");
				e.printStackTrace();
			}
		}
		
		return dbConnection;
	}
	
	public static void closeDBConnection() {
		if(dbConnection != null)
		{
			try {
				dbConnection.close();
			} catch (SQLException e) {
				System.out.println("Error closing database connection");
				e.printStackTrace();
			}
		}
	}
	
	//Add salt
    public static String getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }
    
    public static String get_SHA_512_SecurePassword(String password, String salt)
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
}
