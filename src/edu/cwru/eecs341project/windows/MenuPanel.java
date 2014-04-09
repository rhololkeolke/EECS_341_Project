package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;

import edu.cwru.eecs341project.GlobalState;

public class MenuPanel extends Panel{
	public MenuPanel(boolean back, boolean checkout) {
		super(new Border.Bevel(true), Panel.Orientation.HORISONTAL);
		if(back)
			addComponent(new Label("Back (Esc)"));
		else
			addComponent(new Label("Exit (Esc)"));
		addComponent(new Label("              "));
		if(checkout)
			addComponent(new Label("Checkout (Ctrl-C)"));
		else
			addComponent(new Label("                                 "));
		if(GlobalState.UserRole.ANONYMOUS == GlobalState.getUserRole())
		{
			addComponent(new Label("Login (Ctrl-l)"));
			addComponent(new Label("Register (Ctrl-r)"));
		} else {
			if(GlobalState.getUserRole() == GlobalState.UserRole.DBA) {
				addComponent(new Label("          "));
				addComponent(new Label("DBA  "));
			} else if(GlobalState.getUserRole() == GlobalState.UserRole.EMPLOYEE) {
				addComponent(new Label("        "));				
				addComponent(new Label("Employee  "));
			} else {
				try {
					addComponent(new Label("            "));									
					addComponent(new Label("" + GlobalState.getCustomerNumber() + "  "));
				} catch (Exception e) {
					addComponent(new Label("            "));					
					addComponent(new Label("-1"));
					System.out.println(e);
				}
			}
			addComponent(new Label("Logout (Ctrl-l)"));
		}
	}
}
