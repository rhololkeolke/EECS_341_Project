package edu.cwru.eecs341project.windows;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.layout.LayoutParameter;
import com.googlecode.lanterna.input.Key;

public class MicrocenterWindow extends Window{
	private Panel mainPanel;
	public MicrocenterWindow(final GUIScreen guiScreen, String label, boolean back, boolean checkout, boolean login) {
		super(label);
		mainPanel = new Panel();
		
		mainPanel.addComponent(new MenuPanel(false, false, false));
		
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
        		MessageBox.showMessageBox(guiScreen, "Login", "Not yet implemented");
        	}
        });
        
        mainPanel.addShortcut('r', true, false, new Action() {
        	@Override
        	public void doAction() {
        		MessageBox.showMessageBox(guiScreen, "Register", "Not yet implemented");
        	}
        });
        		
		super.addComponent(mainPanel);
	}
	
	@Override
	public void addComponent(Component component, LayoutParameter... layoutParameters)
    {
		super.removeComponent(mainPanel);
		mainPanel.addComponent(component, layoutParameters);
		super.addComponent(mainPanel);
    }
}
