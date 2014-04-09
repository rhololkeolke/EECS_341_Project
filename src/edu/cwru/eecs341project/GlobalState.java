package edu.cwru.eecs341project;

public class GlobalState {
	
	public enum UserRole {
		ANONYMOUS, CUSTOMER, EMPLOYEE, DBA
	}
	
	private static int customerNumber = -1;
	private static UserRole userRole = UserRole.ANONYMOUS;
	
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
}
