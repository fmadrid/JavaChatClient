// Author: 	Frank Madrid
// Purpose:	CMPS 394 Homework #8 Application of Multithreading, Synchronization, Socket and OOP
//											Design and Implementation
//					- Get familiar with and apply following concepts to build appliations
//							- Apply Multithreading, Thread-Synchronization, Network Programming to solve more complicated problems
//							- Use OOP, top-down design method in program design and implementation.

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.ScrollPane;

import java.net.Socket;

import java.util.Vector;

/* Class Hierarchy
	Class ChatroomAdvisor
		- advisorName: 		String
		- output: 				ObjectOutputStream
		- input: 				ObjectInputStream
		- connection: 			Socket
		- connectionMonitor: Thread
		- promptWindow: 		PromptWindow
		- listWindow: 			ListWindow
		- chatroomWindow: 	Vector <ChatroomWindow>
		
		+ ChatroomAdvisor(): void
		+ main(String[]: args): void
*/
public class ChatroomAdvisor {

	private String advisorName;
	
	private ObjectOutputStream output;	// Reads data from the server
	private ObjectInputStream 	input;	// Writes data to the server
	
	private Socket connection;				// Client to server connection
	private Thread connectionMonitor;	// Reads commands and text from the server
	
	private PromptWindow	  			  promptWindow;	// Frame which prompts an advisor for his username
	private ListWindow				  listWindow;		// Frame displaying a tabular list of clients connected to the server
	private Vector <ChatroomWindow> chatroomWindow;	// Vector of all connected chatroomWindows
	private AdvisorReader			  chatroomMonitor;
	
	// Function - ChatroomAdvisor()
	// Default constructor. Instantiates the class member variables. Initializes the program by instantiating the JFrame 
	// promptWindow object
	public ChatroomAdvisor() {
		
		advisorName = "";
		
		output = null;
		input = null;
		
		connection = null;
		chatroomMonitor = null;
		connectionMonitor = null;
		
		chatroomWindow = null;
		listWindow = null;
		
		promptWindow = new PromptWindow();
		
	}
	
	// Function - main(String [] args)
	// Initializes the program by calling method ChatroomClient().
	public static void main(String [] args) {
	
		new ChatroomAdvisor();
	}
	
	/* Class Hierarchy
		Class PromptWindow extends JFrame implements ActionListener
			- contentPanel:	JPanel
			- statusPanel:		JPanel
			- userNameField:	JTextField
			- zipCodeField:	JTextfield
			- startChatroom:	JButton
			- serverStatus:	JLabel
			
			+ PromptWindow():			void
			- initializeContent(): 	void
			- initializeWindow():	void
			- initializeConnection(String: userName, String: zipCode): void
			- updateStatusMessage(String: message, Color: color):	void
			+ actionPerformed(ActionEvent: event)
	*/
	private class PromptWindow extends JFrame implements ActionListener{
		
		private JPanel contentPanel;			// Houses the username field, the zip code field, and the startChatroom button
		private JPanel statusPanel;			// Houses the server status label
		
		private JTextField advisorNameField;	// Stores the representative's username
		private JButton startChatroom;
		
		private JLabel serverStatus;
		
		// Function - PromptWindow()
		// Default contructor. Instantiates the class member variables.
		public PromptWindow() {
			
			super("Homework #8 Online Help Program: Chatroom Advisor");
			
			initializeContent();
			initializeWindow();
			
		}
		
		// Function - initializeContent()
		// Helper function to PromptWindow. Instantiates the window content.
		private void initializeContent() {
		
			// Initialize the User Name label and text field.
			advisorNameField = new JTextField(10);
			advisorNameField.setText("");
			advisorNameField.addActionListener(this);
			advisorNameField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {}
				
				public void keyReleased(KeyEvent e) {
					if(advisorNameField.getText().length() != 0)
						startChatroom.setEnabled(true);
					if(advisorNameField.getText().length() == 0)
						startChatroom.setEnabled(false);
				}
				
				public void keyTyped(KeyEvent e) {}
				
			});
			
			// Initialize the Connect button
			startChatroom = new JButton("Connect");
			startChatroom.setEnabled(false);
			startChatroom.addActionListener(this);
			
			// Initialize the server status label
			serverStatus = new JLabel();
			updateStatusMessage("Disconnected", Color.RED);
			
		}
		
		// Function - initializeWindow()
		// Helper function to promptWindow(). Instantiates the window and adds its content.
		private void initializeWindow() {
			
			// Initialize the JFrame
			setLayout(new BorderLayout());
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(ChatroomConstants.PROMPT_WINDOW_FRAME_LENGTH, ChatroomConstants.PROMPT_WINDOW_FRAME_HEIGHT);
			
			contentPanel = new JPanel();
			statusPanel = new JPanel();
			
			contentPanel.add(new JLabel("Advisor Name: "));
			contentPanel.add(advisorNameField);
			
			contentPanel.add(startChatroom);
			
			statusPanel.add(new JLabel("Status: "));
			statusPanel.add(serverStatus);
			
			add(contentPanel, BorderLayout.CENTER);
			add(statusPanel, BorderLayout.SOUTH);
			
			setVisible(true);
			
		}
		
		// Function - initializeConnection()
		// Helper function to promptWindow. Establishes a connection to the server on port number 9999,
		// transmits the update user information command, and writes the client's username and zip code.
		private void initializeConnection() {
		
			try {
				
				connection = new Socket("localhost", 9999);
				
				output = new ObjectOutputStream(connection.getOutputStream());
				input = new ObjectInputStream(connection.getInputStream());
				
				updateStatusMessage("Connected", Color.GREEN);
				advisorName = advisorNameField.getText();
				
				output.writeInt(ChatroomConstants.ADVISOR_CONNECTED);
				output.flush();
				
				output.writeUTF(advisorNameField.getText());
				output.flush();
				
				advisorNameField.setEditable(false);
				startChatroom.setEnabled(false);
				
				listWindow = new ListWindow();
				
			} catch(IOException exception) {
				
				updateStatusMessage("Server Unreachable", Color.RED);
				
			}
		}
		
		// Function - updateStatusMessage
		// Updates the text and color of the server status message within the prompt window
		private void updateStatusMessage(String message, Color color) {
			
			serverStatus.setText("");
			serverStatus.setForeground(color);
			serverStatus.setText(message);
			
		}
		
		//Function - actionPerformed
		// Calls initializeConnection().
		public void actionPerformed(ActionEvent event) {
			if(advisorNameField.getText().length() != 0)
				initializeConnection();
				
		}
	}
	
	public class ListWindow extends JFrame implements ActionListener{
	
		private JScrollPane listPanel;
		
		private JTable 				clientTable;
		private DefaultTableModel 	model;
		
		private String[] 		columnNames;
		private String[][]	clientData;
	
		public ListWindow() {
			
			super("Homework #8 Online Help Program: Chatroom Representative");
			
			initializeContent();
			initializeWindow();
			
			chatroomMonitor = new AdvisorReader();
			connectionMonitor = new Thread(chatroomMonitor);
			connectionMonitor.start();
			
		}
		
		private void initializeContent() {
			
			columnNames = new String[] {"Name", "Zip Code", "Representative"};
			getTableData();
			clientData = null;
			clientTable = new JTable(new DefaultTableModel(clientData, columnNames));
			model = (DefaultTableModel) clientTable.getModel();
			
		}
		
		private void initializeWindow() {
		
			setLayout(new BorderLayout());
			listPanel = new JScrollPane(clientTable);
			
			add(listPanel, BorderLayout.CENTER);
			setVisible(true);
			
		}
		
		private void getTableData() {

			int rowCount;
			int columnCount;
			
			String[] row;
			String field;
			
			rowCount = 0;
			columnCount = ChatroomConstants.CLIENT_COLUMN_COUNT;
			
			try {
				output.writeInt(ChatroomConstants.GET_CLIENT_LIST);
				output.flush();
				
				rowCount = input.readInt();
			}
			catch(IOException exception) {}
			
			clientData = new String[rowCount][columnCount];
			
			if(rowCount == 0) {
				clientData = null;
				return;
			}
			
			for(int i = 0; i < rowCount; i++)
				for(int j = 0; j < columnCount; j++) {

					try {

						field = input.readUTF();
						System.out.println(field);
						clientData[i][j] = field;
					}
					catch(IOException exception) {}
				}
				
		}
	
		private void addRow(String[] client) {
		
			model.addRow(client);
			
		}

		public void remove(String clientName) {
		
			int row = -1;
			
			for(int i = 0; i < model.getRowCount(); i++) {
				row = i;
				if(model.getValueAt(row, 0).equals(clientName)) {
					model.removeRow(row);
					return;
				}
			}
			
			return;
		}
		
		public void actionPerformed(ActionEvent event){}
	}
	
	public class ChatroomWindow {}
	
		
	/* Class Hierarchy
		Class AdvisorReader implements Runnable
			- command: 		int
			- string:		String
			- terminate: 	Boolean
			
			+ ClientReader(): void
			+ terminateProcessing(): void
			+ run(): void
	*/
	private class AdvisorReader implements Runnable {
	
		private int command;		// Queried command from the server
		private String string;	// Message read from the server
		private boolean terminate;
		
		// Function - AdvisorReader()
		// Default constructor. Initializes the class member variables
		public AdvisorReader() {
			
			command = -1;
			string = null;
			terminate = false;
			
		}
		
		/* Function - terminateProcessing
		 * Called when the ChatroomWindow has been closed. Terminates the thread.
		 */
		public void terminateProcessing() {
			
			terminate = true;
			
		}
		
		/* Function - run()
		 * Main processing. Waits for a command from the server via the ObjectInputStream.
		 * Command List:
		 *
		 *		ADD_CLIENT
		 */
		public void run() {
		
			while(!terminate) {
				try {
					command = input.readInt();
					switch(command) {
					
						case ChatroomConstants.ADD_CLIENT :
							
							String[] client = new String[3];
							client[0] = input.readUTF();
							client[1] = input.readUTF();
							client[2] = input.readUTF();
							listWindow.addRow(client);
							
							break;
						
						case ChatroomConstants.REMOVE_CLIENT :
						
							string = input.readUTF();
							
							listWindow.remove(string);
							
							break;
					}
				} catch(IOException exception) {}
			}
		}
	}
}