package edu.cwru.eecs341project;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;

import edu.cwru.eecs341project.windows.MainWindow;


public class Main {
	public static void main(String[] args) {
		 GUIScreen textGUI = TerminalFacade.createGUIScreen();
		    if(textGUI == null) {
		        System.err.println("Couldn't allocate a terminal!");
		        return;
		    }
		    textGUI.getScreen().startScreen();
		    
		    textGUI.showWindow(new MainWindow(textGUI), GUIScreen.Position.FULL_SCREEN);
		    
		    textGUI.getScreen().stopScreen();
	 }
}
