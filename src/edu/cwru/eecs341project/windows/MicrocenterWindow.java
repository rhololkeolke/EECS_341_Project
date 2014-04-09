package edu.cwru.eecs341project.windows;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;
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
        		if(GlobalState.getUserRole() == GlobalState.UserRole.ANONYMOUS)
        		{
        			MessageBox.showMessageBox(guiScreen, "Login", "No user accounts setup. Automatically logging in as DBA");
        			GlobalState.setUserRole(GlobalState.UserRole.DBA);
        		} else {
        			GlobalState.setUserRole(GlobalState.UserRole.ANONYMOUS);
        			WindowManager.exitToMain();
        		}
        		WindowManager.refreshWindow();
        	}
        });
        
        mainPanel.addShortcut('r', true, false, new Action() {
        	@Override
        	public void doAction() {
        		MessageBox.showMessageBox(guiScreen, "Register", "Not yet implemented");
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
}
