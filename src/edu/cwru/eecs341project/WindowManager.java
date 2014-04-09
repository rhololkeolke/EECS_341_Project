package edu.cwru.eecs341project;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.cwru.eecs341project.windows.ManagedWindow;

public class WindowManager {
	private static Stack<ManagedWindow> windows = new Stack<ManagedWindow>();
	
	public static void pushWindow(ManagedWindow window){
		windows.push(window);
	}
	
	public static void popWindow() {
		windows.pop();
	}
	
	public static void refreshWindow() {
		windows.peek().refresh();
	}
}
