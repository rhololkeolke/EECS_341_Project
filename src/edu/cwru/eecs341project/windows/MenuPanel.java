package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;

public class MenuPanel extends Panel{
	public MenuPanel(boolean back, boolean checkout, boolean login) {
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
		if(login)
		{
			addComponent(new Label("Login (Ctrl-l)"));
			addComponent(new Label("Register (Ctrl-r)"));
		} else {
			addComponent(new Label("                "));
			addComponent(new Label("Logout (Ctrl-l)"));
		}
	}
}
