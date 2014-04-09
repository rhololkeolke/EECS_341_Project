package edu.cwru.eecs341project;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;


public class Main {
	public static void main(String[] args) {
		 GUIScreen textGUI = TerminalFacade.createGUIScreen();
		    if(textGUI == null) {
		        System.err.println("Couldn't allocate a terminal!");
		        return;
		    }
		    textGUI.getScreen().startScreen();

		    try {
		    	Thread.sleep(5000);
		    } catch(InterruptedException e) {
		    	textGUI.getScreen().stopScreen();
		    	System.exit(0);
		    }
		    
		    textGUI.getScreen().stopScreen();
	 }
}
