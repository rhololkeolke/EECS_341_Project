package edu.cwru.eecs341project;

import java.util.Stack;

import edu.cwru.eecs341project.windows.ManagedWindow;

public class WindowManager {
	private static Stack<ManagedWindow> windows = new Stack<ManagedWindow>();
	
	public static void pushWindow(ManagedWindow window){
		windows.push(window);
	}
	
	public static void popWindow() {
		windows.pop();
		if(windows.size() > 0)
			windows.peek().refresh();
	}
	
	public static int stackSize() {
		return windows.size();
	}
	
	public static void exitToMain() {
		while(windows.size() > 1)
		{
			windows.peek().close();
		}
	}
	
	public static void refreshWindow() {
		windows.peek().refresh();
	}
	
	public static void refreshAllWindows() {
		for(ManagedWindow window : windows)
		{
			window.refresh();
		}
	}
}
