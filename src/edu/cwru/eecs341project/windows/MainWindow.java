package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;

public class MainWindow extends Window {
	public MainWindow() {
		super("Microcenter Store Application");
		addComponent(new Button("Login"));
		addComponent(new Button("Logout"));
		addComponent(new Button("Register"));
		addComponent(new Button("User Management"));
		addComponent(new Button("Stores"));
		addComponent(new Button("Products"));
		addComponent(new Button("Customers"));
		addComponent(new Button("Checkout"));
		addComponent(new Button("Database"));
	}
}
