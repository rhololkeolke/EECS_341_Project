package edu.cwru.eecs341project;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class GlobalState {
	
	public enum UserRole {
		ANONYMOUS, CUSTOMER, EMPLOYEE, DBA
	}
	
	private static int customerNumber = -1;
	private static UserRole userRole = UserRole.ANONYMOUS;
	private static Connection dbConnection = null;
	
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
}
