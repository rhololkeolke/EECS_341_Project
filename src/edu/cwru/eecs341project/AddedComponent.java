package edu.cwru.eecs341project;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.layout.LayoutParameter;

public class AddedComponent {

	public Component component;
	public LayoutParameter[] layoutParameters;

	public AddedComponent(Component component, LayoutParameter[] layoutParameters)
	{
		this.component = component;
		this.layoutParameters = layoutParameters;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj == component) return true; // this is why I needed to override the method
		if(obj == this) return true;
		return false;
	}
}
