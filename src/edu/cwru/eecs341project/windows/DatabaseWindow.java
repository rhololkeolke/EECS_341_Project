package edu.cwru.eecs341project.windows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EditArea;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.gui.dialog.MessageBox;

import edu.cwru.eecs341project.GlobalState;
import edu.cwru.eecs341project.WindowManager;

public class DatabaseWindow extends MicrocenterWindow {
	
	private Panel actionsPanel;
	
	public DatabaseWindow(GUIScreen guiScreen, String label) {
		super(guiScreen, label, true, false);
		
		actionsPanel = new Panel();
		ActionListBox actionListBox = new ActionListBox();
		actionListBox.addAction(new ActionListBoxItem("Raw Query"));
		actionListBox.addAction(new ActionListBoxItem("Show Tables"));
		actionListBox.addAction(new ActionListBoxItem("Show Table Schema"));
		actionsPanel.addComponent(actionListBox);
        addComponent(actionsPanel);
	}
	
	private class RawQueryWindow extends MicrocenterWindow {
		private Panel inputPanel;
		private EditArea queryBox;
		private TextArea queryResult;
		public RawQueryWindow(final GUIScreen guiScreen)
		{
			super(guiScreen, "Raw SQL Query", true, false);
			
			inputPanel = new Panel();
			inputPanel.addComponent(new Label("Enter a query to execute"));
			queryBox = new EditArea(guiScreen.getScreen().getTerminalSize(), "SELECT * FROM customer LIMIT 10;");
			inputPanel.addComponent(queryBox);
	
			inputPanel.addComponent(new Button("Execute", new Action() {
				@Override
				public void doAction() {					
					Connection dbConnection = GlobalState.getDBConnection();
					String result = "";
					try {
						Statement st = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
						ResultSet queryResult = st.executeQuery(queryBox.getData());
						result = resultsToString(queryResult);
						queryResult.close();
						st.close();
					} catch(SQLException e) {
						result = "ERROR executing query:\n" + e.getMessage();
					}
					
					inputPanel.removeComponent(queryResult);
					queryResult = new TextArea(guiScreen.getScreen().getTerminalSize(), result);
					inputPanel.addComponent(queryResult);
					WindowManager.refreshWindow();
				}
			}));
			addComponent(inputPanel);
		}
		
		private String resultsToString(ResultSet rs) throws SQLException{
			ResultSetMetaData rsmd = rs.getMetaData();
			int[] maxColLength = new int[rsmd.getColumnCount()];
			for(int i=1; i<=rsmd.getColumnCount(); i++)
			{
				maxColLength[i-1] = rsmd.getColumnLabel(i).length();
			}
			
			while(rs.next())
			{
				for(int i=1; i<=rsmd.getColumnCount(); i++)
				{
					String columnText = rs.getString(i);
					if(columnText != null)
						maxColLength[i-1] = Math.max(maxColLength[i-1], rs.getString(i).length());
				}
			}
			rs.beforeFirst(); // reset the result cursor
			StringBuilder sb = new StringBuilder();
			sb.append("|");
			for(int i=1; i<=rsmd.getColumnCount(); i++)
			{
				sb.append(" " + rsmd.getColumnLabel(i));
				for(int j=0; j<maxColLength[i-1] - rsmd.getColumnLabel(i).length(); j++)
				{
					sb.append(" ");
				}
				sb.append(" |");
			}
			sb.append("\n");
			sb.append("-");
			for(int i=1; i<=rsmd.getColumnCount(); i++)
			{
				for(int j=0; j<maxColLength[i-1] + 3; j++)
				{
					sb.append("-");
				}
			}
			sb.append("\n");
			
			while(rs.next())
			{
				sb.append("|");
				for(int i=1; i<=rsmd.getColumnCount(); i++)
				{
					String columnText = rs.getString(i);
					int columnLength = 0;
					if(columnText == null)
					{
						sb.append(" ");
						columnLength = 0;
					} else {
						sb.append(" " + rs.getString(i));
						columnLength = columnText.length();
					}
					for(int j=0; j<maxColLength[i-1] - columnLength; j++)
					{
						sb.append(" ");
					}
					sb.append(" |");
				}
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
	
	private class ActionListBoxItem implements Action {
        private String label;

        public ActionListBoxItem(String label) {
            this.label = label;
        }
        
        @Override
        public String toString() {
            return label;
        }

        public void doAction() {
        	if(label.equals("Raw Query")) {
        		//TextInputDialog.showTextInputBox(guiScreen, "SQL Query", "Enter query to execute", "SELECT * FROM customer LIMIT 10");
        		guiScreen.showWindow(new RawQueryWindow(guiScreen), GUIScreen.Position.FULL_SCREEN);
        	} else if(label.equals("Show Tables")) {
        		
        	} else if(label.equals("Show Table Schmea")) {
        		
        	}
        }
	}
}
