package edu.cwru.eecs341project;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.gui.Window;

import edu.cwru.eecs341project.windows.ManagedWindow;

public class WindowManager {
	private static List<ManagedWindow> windows = new ArrayList<ManagedWindow>();
	
	public static void addWindow(ManagedWindow window){
		windows.add(window);
	}
	
	public static void refreshAllWindows() {
		for(ManagedWindow window : windows)
		{
			window.refresh();
		}
	}
}
