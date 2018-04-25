// Author: 	Frank Madrid
// Purpose:	CMPS 394 Homework #8 Application of Multithreading, Synchronization, Socket and OOP
//											Design and Implementation
//					- Get familiar with and apply following concepts to build appliations
//							- Apply Multithreading, Thread-Synchronization, Network Programming to solve more complicated problems
//							- Use OOP, top-down design method in program design and implementation.

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JLabel;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.ScrollPane;

import java.net.Socket;

/* Class Hierarchy
	Class ChatRoomClient extends JFrame implements ActionListener
		- output:				ObjectOutputStream
		- input:					ObjectInputStream
		- connection:			Socket
		- connectionMonitor:	Thread
		- chatroomWindow:		ChatroomWindow
		- promptWindow:		PromptWindow
		- chatroomMonitor:	ClientReader
		
		+ ChatRoomClient(): 	void
		+ main():				void
*/
public class ChatroomClient {
	
	private String userName;
	
	private ObjectOutputStream output;			// Reads data from the server
	private ObjectInputStream 	input;			// Writes data to the server
	
	private Socket connection;						// Client to server connection
	private Thread connectionMonitor;			// Reads commands and text from the server
	
	private ChatroomWindow	chatroomWindow;	// Frame which allows an advisor and client to communicate
	private PromptWindow		promptWindow;		// Frame which prompts for a username and zip code
	private ClientReader		chatroomMonitor;	// Monitors activity on the ObjectInputStream
	
	/* Function - ChatroomClient()
	 * Default constructor. Instantiates the class member variables. Initiates the GUI by isntantiating promptWindow
	 */
	public ChatroomClient() {
		
		userName = null;
		
		output = null;
		input = null;
		
		connection = null;
		connectionMonitor = null;
		
		chatroomWindow = null;
		chatroomMonitor = null;
		promptWindow = new PromptWindow();
		
	}
	
	/* Function - main(String [] args)
	 * Initializes the program by calling method ChatroomClient().
	 */
	public static void main(String [] args) {
	
		new ChatroomClient();
	}
	
	/* Class Hierarchy
		Class PromptWindow extends JFrame implements ActionListener
			- contentPanel:	JPanel
			- statusPanel:		JPanel
			- userNameField:	JTextField
			- zipCodeField:	JTextfield
			- startChatroom:	JButton
			- serverStatus:	JLabel
			
			+ PromptWindow(): void
			- initializeContent(): void
			- initializeWindow(): void
			- initializeConnection(): void
			- updateStatusMessage(String: message, Color: color):	void
			+ actionPerformed(ActionEvent: event): void
	*/
	private class PromptWindow extends JFrame implements ActionListener{
		
		private JPanel contentPanel;			// Houses the username field, the zip code field, and the startChatroom button
		private JPanel statusPanel;			// Houses the server status label
		
		private JTextField userNameField;	// Stores the client's username
		private JTextField zipCodeField;		// Stores the client's zip code
		private JButton startChatroom;		// Begins the initiation of the connection to the server and the ChatroomWindow
		private JLabel serverStatus;			// Stores the server status.
														//		Server is Unreachables
														//			- The client has unsuccessfull connected to the server
														//		Connected
														//			- The client has successfully connected to the server
														//		Disconnected
														//			- The client is not connected to the server
		
		/* Function - PromptWindow()
		 * Default contructor. Initializes the username field, the zip code field, and the connect button of the log-in
		 * window. If a valid username and zip code has been entered, enable the connect button.
		 */
		public PromptWindow() {
			
			super("Homework #8 Online Help Program: Chatroom Client");
			
			initializeContent();
			initializeWindow();
			
		}
		
		/* Function - initializeContent()
		 * Helper function to promptWindow(). Ininitialize the window content.
		 */
		private void initializeContent() {
		
			// Initialize the User Name label and text field.
			userNameField = new JTextField(10);
			userNameField.setText("");
			userNameField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {}
				
				public void keyReleased(KeyEvent e) {
					if(userNameField.getText().length() != 0 && zipCodeField.getText().length() >= 5)
						startChatroom.setEnabled(true);
					if(userNameField.getText().length() == 0 || zipCodeField.getText().length() < 5)
						startChatroom.setEnabled(false);
				}
				
				public void keyTyped(KeyEvent e) {}
				
			});
			
			// Initialize the Zip Code label and text field.
			zipCodeField = new JTextField(10);
			zipCodeField.setText("");
			zipCodeField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {}
				
				public void keyReleased(KeyEvent e) {
					if(userNameField.getText().length() != 0 && zipCodeField.getText().length() >= 5)
						startChatroom.setEnabled(true);
					if(userNameField.getText().length() == 0 || zipCodeField.getText().length() < 5)
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
		
		/* Function - initializeWindow()
		 * Helper function to promptWindow(). Defines the container, and frame, and panel properties.
		 */
		private void initializeWindow() {
			
			// Initialize the JFrame
			setLayout(new BorderLayout());
			setSize(ChatroomConstants.PROMPT_WINDOW_FRAME_LENGTH, ChatroomConstants.PROMPT_WINDOW_FRAME_HEIGHT);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			contentPanel = new JPanel();
			statusPanel = new JPanel();
			
			contentPanel.add(new JLabel("Username: "));
			contentPanel.add(userNameField);
			
			contentPanel.add(new JLabel("Zip Code: "));
			contentPanel.add(zipCodeField);
			
			contentPanel.add(startChatroom);
			
			statusPanel.add(new JLabel("Status: "));
			statusPanel.add(serverStatus);
			
			add(contentPanel, BorderLayout.CENTER);
			add(statusPanel, BorderLayout.SOUTH);
		
			setVisible(true);
		}
		
		/* Function - initializeConnection()
		 * Called from function actionPerformed(ActionEvent event). Establishes a connection to the server on port number 
		 * 9999, broadcasts the CLIENT_CONNECTED command to the server, and then transmits the advisor's username.
		 */
		private void initializeConnection() {
		
			try {
			
				connection = new Socket("localhost", 9999);
				
				output = new ObjectOutputStream(connection.getOutputStream());
				input = new ObjectInputStream(connection.getInputStream());
				
				updateStatusMessage("Connected", Color.GREEN);
				userName = userNameField.getText();
				
				output.writeInt(ChatroomConstants.CLIENT_CONNECTED);
				output.flush();
				
				output.writeUTF(userNameField.getText());
				output.flush();
				
				output.writeUTF(zipCodeField.getText());
				output.flush();
				
				userNameField.setEditable(false);
				zipCodeField.setEditable(false);
				startChatroom.setEnabled(false);
				
				chatroomWindow = new ChatroomWindow();
				
			} catch(IOException exception) {
			
				updateStatusMessage("Server Unreachable", Color.RED);
			
			}
		}
		
		/* Function - updateStatusMessage
		/* Updates the text and color of the server status message within the prompt window
		 */
		private void updateStatusMessage(String message, Color color) {
			
			serverStatus.setText("");
			serverStatus.setForeground(color);
			serverStatus.setText(message);
			
		}
		
		/* Function - actionPerformed
		 * If a valid username and zip code has been entered into the respective fields, clicking "Connect" will call
		 * function initializeConnection().
		 */
		public void actionPerformed(ActionEvent event) {
			
			if(userNameField.getText().length() != 0 && zipCodeField.getText().length() >= 5)
				initializeConnection();
				
		}
	}
	
	/* Class Hierarchy
		Class ChatroomWindow extends JFrame implements ActionListener
			- statusPanel: 	JPanel
	 		- messagePanel: 	JPanel
	 		- questionPanel: 	JPanel
	 		- advisorName: 	JPanel
	 		- advisorStatus: 	JLabel
	 		- messageContent:	JTextPane
	 		- document:			StyledDocument
	 		- style:				SimpleAttributeSet
	 		- questionField:	JTextField
	 		- sendMessage:		JButton
	 
			+ ChatroomWindow(): void
			- initializeContent(): void
			- initializeWindow(): void
			- updateAdvisorName(String: message, Color: color): void
			- updateAdvisorStatus(String: message, Color: color): void
			- writeServerMessage(String: message, ColoR: color): void
			- writeClientMessage(String: message, Color: color): void
			- writeAdvisorMessage(String: message, Color:color): void
	*/
	private class ChatroomWindow extends JFrame implements ActionListener{
	
		private JPanel 		statusPanel;				// Displays the advisor name and status
		private JScrollPane 	messagePanel;				// Displays the message history
		private JPanel 		questionPanel;				// Displays the question field and send button
		
		private JLabel advisorName;						// Stores the representative's name
		private JLabel advisorStatus;						// Stores the representative's status
																	//		Disconnected
																	//			No advisor is connected to the chatroom
																	//		Connected
																	//			An advisor has connected to the chatroom
																	//		is typing...
																	//			The advisor is currently typing a response
		
		private JTextPane 			messageContent;	//	Stores the message history
		private StyledDocument 		document;			//	Stores the text
		private SimpleAttributeSet style;				//	Defines the style at which the text is printed to the message Area
		
		private JTextField questionField;				// Stores the client's question
		private JButton 	 sendMessage;					// Button which sends the question to the server
		
		/* Function - ChatroomWindow()
		 * Default constructor. Initialies the message area, question field, and send button of the chatroom window. Uses
		 * connectionMonitor to send and receive commands. Begins the ChatMonitor processing.
		 */
		public ChatroomWindow() {
		
			super("Homework #8 Online Help Program: Chatroom Client");
			
			initializeContent();
			initializeWindow();
			
			chatroomMonitor = new ClientReader();
			connectionMonitor = new Thread(chatroomMonitor);
			connectionMonitor.start();
		
		}
		
		/* Function - initializeContent()
		 * Helper function to ChatroomWindow. Initializes the window content.
		 */
		private void initializeContent() {
		
			// Initializes the advisor name and status labesl
			advisorName = new JLabel();
			updateAdvisorName("None", Color.RED);
			
			advisorStatus = new JLabel();
			updateAdvisorStatus("Waiting for a representative", Color.RED);
			
			// Initialize the message panel
			messageContent = new JTextPane();
			messageContent.setEditable(false);
			document = messageContent.getStyledDocument();
			style = new SimpleAttributeSet();
			messagePanel = new JScrollPane(messageContent);
			
			// Initialize the question label and text field
			questionField = new JTextField(50);
			questionField.setText("");
			questionField.setEditable(false);
			questionField.addActionListener(this);
			
			// Initialize the send message button
			sendMessage = new JButton("Send");
			sendMessage.setEnabled(false);
			
		}
			
		/* Function - initializeWindow()
		 * Helper function to ChatRoomWindow. Defines the window properties.
		 */
		private void initializeWindow() {
		
			// Initialize the JFrame
			setLayout(new BorderLayout());
			setSize(ChatroomConstants.CHATROOM_WINDOW_FRAME_LENGTH, ChatroomConstants.CHATROOM_WINDOW_FRAME_HEIGHT);
			setLocationRelativeTo(null);
			setResizable(false);
			
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					try {

						output.writeInt(ChatroomConstants.CLIENT_DISCONNECTED);
						output.flush();
						
						output.writeUTF(userName);
						output.flush();
						
						chatroomMonitor.terminateProcessing();
						input.close();
						output.close();
						connection.close();
						
						promptWindow.updateStatusMessage("Disconnected", Color.RED);
						
						promptWindow.userNameField.setEditable(true);
						promptWindow.zipCodeField.setEditable(true);
						promptWindow.startChatroom.setEnabled(true);
						
						dispose();
					}
					catch(Exception exception){}
				}
			});
			
			statusPanel = new JPanel();
			messageContent = new JTextPane();
			questionPanel = new JPanel();
			
			statusPanel.add(new JLabel("Representative: "));
			statusPanel.add(advisorName);
			statusPanel.add(new JLabel("Status: "));
			statusPanel.add(advisorStatus);
			
			writeServerMessage("Welcome to the Online Help Chat Room. Please wait for the next available representative.");
			
			questionPanel.add(new JLabel("Question: "));
			questionPanel.add(questionField);
			questionPanel.add(sendMessage);
			
			add(statusPanel, BorderLayout.NORTH);
			add(messagePanel, BorderLayout.CENTER);
			add(questionPanel, BorderLayout.SOUTH);
			
			setVisible(true);

		}

		/* Function - updateStatusMessage
		 * Updates the text and color of the server status message within the prompt window
		 */
		private void updateAdvisorName(String message, Color color) {
			
			advisorName.setText("");
			advisorName.setForeground(color);
			advisorName.setText(message);
			
		}
		
		/* Function - updateStatusMessage
		 * Updates the text and color of the server status message within the prompt window
		 */
		private void updateAdvisorStatus(String message, Color color) {
			
			advisorStatus.setText("");
			advisorStatus.setForeground(color);
			advisorStatus.setText(message);
			
		}
	
		/* Function - writeServerMessage(String message)
		 * Writes a message from the server to the message area
		 */
		private void writeServerMessage(String message) {
			
			StyleConstants.setBackground(style, Color.WHITE);
			StyleConstants.setForeground(style, Color.RED);
			StyleConstants.setBold(style, true);
			StyleConstants.setItalic(style, false);
			StyleConstants.setUnderline(style, false);
			
			try {document.insertString(document.getLength(), message + "\n", style);}
			catch(BadLocationException exception) {}
			
		}
		
		/* Function writeClientMessage(String message)
		 * Writes the question from the user onto the message area
		 */
		private void writeClientMessage(String message) {
			
			StyleConstants.setBackground(style, Color.WHITE);
			StyleConstants.setForeground(style, Color.BLACK);
			StyleConstants.setBold(style, false);
			StyleConstants.setItalic(style, false);
			StyleConstants.setUnderline(style, false);
			
			try {document.insertString(document.getLength(), userName + ": " + message + "\n", style);}
			catch(BadLocationException exception) {}
			
		}
		
		/* Function writeAdvisorMessage(String message)
		 * Writes the answer from the advisor onto the message area
		 */
		private void writeAdvisorMessage(String message) {
			
			StyleConstants.setBackground(style, Color.WHITE);
			StyleConstants.setForeground(style, Color.BLUE);
			StyleConstants.setBold(style, false);
			StyleConstants.setItalic(style, true);
			StyleConstants.setUnderline(style, false);
			
			try {document.insertString(document.getLength(), advisorName + ": " + message + "\n", style);}
			catch(BadLocationException exception) {}
			
		}
		
		/* Function - actionPerformed(ActionEvent event)
		 * If an advisor is within the room, and a valid question has been entered, clicking "Send" will transmit the 
		 * WRITE_MESSAGE command and question to the server. 
		 */
		public void actionPerformed(ActionEvent event) {
		
			try {
			
				output.writeInt(ChatroomConstants.WRITE_MESSAGE);
				output.flush();
				
				output.writeUTF(questionField.getText());
				output.flush();
				
				writeClientMessage(questionField.getText());
				
				questionField.setText("");
				
			} catch(IOException exception) {}
		}
	}
	
	/* Class Hierarchy
		Class ClientRead implements Runnable
			- command: 		int
			- string:		String
			- terminate: 	Boolean
			
			+ ClientReader(): void
			+ terminateProcessing(): void
			+ run(): void
	*/
	private class ClientReader implements Runnable {
	
		private int command;		// Queried command from the server
		private String string;	// Message read from the server
		private boolean terminate;
		
		// Function - ClientReader()
		// Default constructor. Initializes the class member variables
		public ClientReader() {
			
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
		 *		ADVISOR_CONNECTED
		 *			Signals to the client that an advisor has joined the chatroom.
		 *				- Reads the advisor name from the ObjectInputStream
		 *				- Sets the advisor name to the passed name
		 *				- Sets the advisor status to "Connected"
		 *				- Enables the question field and send message button
		 *
		 *		ADVISOR_DISCONNECTED
		 *			Signals to the client that the advisor has left the chatroom
		 *				- Sets the advisor name to "None"
		 *				- Sets the advisor status to "Disconnected"
		 *	
		 *		UPDATE_ADVISOR_STATUS
		 *			Signals to the client that the advisor is currently typing.
		 *				- Reads the advisor status from the ObjectInputStream
		 *				- Sets the advisor status to the passed status
		 *
		 *		WRITE_MESSAGE
		 *			Signals to the client that the advisor has inputed a message
		 *				- Adds the message to the message display area
		 */
		public void run() {
		
			while(!terminate) {
				try {
				
					command = input.readInt();
					
					switch(command) {
					
						case ChatroomConstants.ADVISOR_CONNECTED : 
									
									string = input.readUTF();
									chatroomWindow.updateAdvisorName(string, Color.BLUE);
									chatroomWindow.updateAdvisorStatus("Connected", Color.BLUE);
									chatroomWindow.writeServerMessage("Advisor " + string + " has joined the room.\n");
									
									chatroomWindow.questionField.setEditable(true);
									chatroomWindow.sendMessage.setEnabled(true);
									break;
									
						case ChatroomConstants.ADVISOR_DISCONNECTED :
									
									chatroomWindow.updateAdvisorStatus("Disconnected", Color.RED);
									chatroomWindow.writeServerMessage("Advisor " + string + " has left the room.\n");
									
									chatroomWindow.questionField.setEditable(false);
									chatroomWindow.sendMessage.setEnabled(false);
									break;
									
						case ChatroomConstants.UPDATE_ADVISOR_STATUS : 
									
									string = input.readUTF();
									chatroomWindow.updateAdvisorStatus(string, Color.BLUE);
									break;
									
						case ChatroomConstants.WRITE_MESSAGE : 
									
									string = input.readUTF();
									chatroomWindow.writeAdvisorMessage(string);
									break;
					}
				} catch(IOException exception) {}
			}
		}
	}
}