// Author: 	Frank Madrid
// Purpose:	CMPS 394 Homework #8 Application of Multithreading, Synchronization, Socket and OOP
//											Design and Implementation
//					- Get familiar with and apply following concepts to build appliations
//							- Apply Multithreading, Thread-Synchronization, Network Programming to solve more complicated problems
//							- Use OOP, top-down design method in program design and implementation.

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.net.Socket;
import java.net.ServerSocket;

import java.util.Vector;

public class ChatroomServer{

	private ChatroomServer chatroomServer;
	private Socket connection;								// Connection between the server and the client/advisor
	private ServerSocket server;							// The ServerSocket which clients/advisors may connect to
	
	private Vector <ChatroomMonitor> chatrooms;		// Stores the list of Monitor object which handle ChatRoom functions
	private Vector <Object[]> connectedClients;		// Stores the list of connected clients
	private Vector <Object[]> connectedAdvisors;		// Stores the list of connected advisors
	
	int roomCount;		// Number of unique rooms which have been instantiated since the server start
	
	private ServerSocketAgent clientHandler;			// Thread which constantly monitors activity from the client/advisor
		
	/* Function - ChatRoomServer()
	 * Default constructor. Initiates the server socket at port 9999 and the ChatroomMonitor object. The ChatroomServer
	 * object then constantly waits for a client/advisor to connect to the ServerSocket via the Socket object. Whenever a 
	 * client/advisor connects, the ChatroomServer object instantiates a new ServerSocketAgent passing the Socket which
	 * connects the server to the new client/advisor.
	 */
	public ChatroomServer() {
		
		chatroomServer = this;
		
		// Server Log
		System.out.println("SERVER: The server is starting up.\n");
		
		try {
			
			// Server Log
			System.out.println("\tInitializing ServerSocket.\n");
			
			server = new ServerSocket(9999);
		}
		catch(Exception exception) {
			
			// Server Log
			System.out.println("\tError: Could not initiate server.\n");
			System.out.println("Exiting the program.\n");
			
			System.exit(0);
		}
		
		chatrooms = new Vector <ChatroomMonitor> (10);
		connectedClients = new Vector <Object[]> (10);
		connectedAdvisors = new Vector <Object[]> (10);
		
		roomCount = 0;
		
		for(int i = 0; i < chatrooms.size(); i++)
			chatrooms.add(new ChatroomMonitor());
			
		while(true) {
			
			try{connection = server.accept();}
			catch(Exception exception){}
			
			clientHandler = new ServerSocketAgent(connection);
			
		}
	}
	
	/* Function - addClient(Object[] connectedClient)
	 * Adds the newly connected client to the connectedClients vector object and broadcasts the ADD_CLIENT command, 
	 * the connected client's Username, and the connected client's Zip Code to each advisor.
	 */
	private synchronized void addClient(Object[] client) {
			
			Object[] advisor;					// Holds an element of the connectedAdvisors vector
			ObjectOutputStream output;		// Advisor's ObjectOutputStream object located at advisor[0]
			
			connectedClients.add(client);
			
			// For each element within the connectedAdvisor vector, write the username and zip code to the element's
			// ObjectOutputStream.
			for(int i = 0; i < connectedAdvisors.size(); i++) {
				
				advisor = connectedAdvisors.elementAt(i);
				output = (ObjectOutputStream) advisor[0];
				try {
					output.writeInt(ChatroomConstants.ADD_CLIENT);	// Send command ADD_CLIENT
					output.flush();
					
					output.writeUTF((String) client[1]);				// Write the Username
					output.flush();
					
					output.writeUTF((String) client[2]);				// Write the Zip Code
					output.flush();
					
					output.writeUTF((String) client[3]);				// Write the advisor currently helping the client
					output.flush();
				}
				catch(IOException exception) {
					System.out.printf("Error: Could not write to the advisor at index %d.\n", i);
				}
			}
				
			// Server Log
			System.out.printf("SERVER: Client %s has logged into the server.\n", client[1]);
			
		}
	
	/* Function - removeClient(Object[] connectedClient)
	 * Removes the client from the connectedClients vector and broadcasts the REMOVE_CLIENT command and the connected
	 * client's Username.
	 */
	 private synchronized void removeClient(Object[] client) {
	 
		Object[] advisor;					// Holds an element of the connectedAdvisors vector
		ObjectOutputStream output;		// Advisor's ObjectOutputStream object located at advisor[0]
		connectedClients.remove(client);
		
		// For each element within the connectedAdvisors vector, write the username and zip code to the element's
		// ObjectOutputStream.
		for(int i = 0; i < connectedAdvisors.size(); i++) {
			advisor = connectedAdvisors.elementAt(i);
			output = (ObjectOutputStream) advisor[0];
			try {
				output.writeInt(ChatroomConstants.REMOVE_CLIENT);	// Send command REMOVE_CLIENT
				output.flush();
				
				output.writeUTF((String) client[1]);					// Write the Username
				output.flush();
			}
			catch(Exception exception) {
				System.out.printf("Error: Could not write to the advisor at index %d.\n", i);
			}
		}

		// Server Log
		System.out.printf("SERVER: Client %s has logged off the server.\n", (String) client[1]);
		
	}
	
	/* Function - addAdvisor(Object[] connectedAdvisor)
	 * Adds the newly connected advisor to the connectedAdvisors vector object.
	 */
	private synchronized void addAdvisor(Object[] advisor) {
		
		connectedAdvisors.add(advisor);
		
		// Server Log
		System.out.printf("SERVER: Advisor %s has logged into the server.\n", advisor[1]);
		
	}
	
	/* Function - removeAdvisor(Object[] connectedAdvisor)
	 * Removes the advisor from the connectedAdvisors vector.
	 */
	private synchronized void removedAdvisor(Object[] advisor) {
		
		connectedAdvisors.remove(advisor);
		
		// Server Log
		System.out.printf("SERVER: Advisor %s has logged into the server.\n", advisor[1]);
		
	}	
	
	/* Function - addChatRoom
	 * Instantiates a new chatroom.
	 */
	private synchronized void addChatroom() {
		
		chatrooms.add(new ChatroomMonitor());
		
	}
	
	/* Function - removeChatRoom
	 * If the passed ChatroomMonitor object is empty of all clients and advisors, remoce the chatroom from the chatrooms
	 * vector list
	 */
	private synchronized void removeChatRoom(ChatroomMonitor chatroom) {
	
		if(chatroom.isEmpty())
			chatrooms.remove(chatroom);
			
	}
	
	/* Function - roomChange(Object[] client, int roomNumber)
	 * Sends the CHANGE_ROOM command and room number to each advisor.
	 */
	private synchronized void roomChange(Object[] client, int roomNumber) {
		
		Object[] advisor;					// Holds an element of the connectedAdvisors vector
		ObjectOutputStream output;		// Advisor's ObjectOutputStream object located at advisor[0]
		
		// For each element within the connectedAdvisor vector, write the username and new room number to the 
		// ObjectOutputStream
			for(int i = 0; i < connectedAdvisors.size(); i++) {
				
				advisor = connectedAdvisors.elementAt(i);
				output = (ObjectOutputStream) advisor[0];
				
				try {
					output.writeInt(ChatroomConstants.CLIENT_ROOM_CHANGE);	// Send command CHANGE_ROOM
					output.writeUTF((String) client[1]);							// Write the Username
					output.writeInt(roomNumber);										// Write the room number
				}
				catch(IOException exception) {
					System.out.printf("Error: Could not write to the advisor at index %d.\n", i);
				}
			}
		}
	
	/* Function - getClient(String username)
	 * Accepts a client's username and returns the associated Object[] client within the connectedClients vector list
	 */
	private synchronized Object[] getClient(String username) {
	
		Object[] client;
		
		for(int i = 0; i < connectedClients.size(); i++) {
			client = connectedClients.elementAt(i);
			
			if(client[1].equals(username)) {
				return client;
			}
				
		}
		
		return null;
		
	}
	
	/* Function - getAdvisor(String username)
	 * Accepts a advisor's username and returns the associated Object[] advisor within the connectedAdvisors vector list
	 */
	private synchronized Object[] getAdvisor(String username) {
	
		Object[] advisor;
		
		for(int i = 0; i < connectedAdvisors.size(); i++) {
			
			advisor = connectedAdvisors.elementAt(i);
			
			if(advisor[1] == username)
				return advisor;
				
		}
		
		return null;
		
	}
	
	// Function - main(String[] args)
	// Instantiates a new ChatroomServer object.
	public static void main(String [] args) {
		
		new ChatroomServer();
		
	}
	
	/* Class ChatroomMonitor
	 * Monitors the activity within the unique chatroom. This class is responsible for adding and removing clients/advisors as
	 * they connect or disconnect from the server and for broadcasting messages to the chatroom.
	 */
	class ChatroomMonitor {

		private int roomID;
		
		private Vector <Object[]> chatroomClients;
		private Vector <Object[]> chatroomAdvisors;

		// Function - ChatRoomMonitor()
		// Default constructor. Instantiates the chatroom member lists.
		public ChatroomMonitor() {

			roomID = roomCount ++;
			chatroomClients = new Vector <Object[]> (10);
			chatroomAdvisors = new Vector <Object[]> (10);
			
		}
		
		/* Function - addClient(Object[] client)
		 * Adds the newly connected client to the chatroomClients vector object and broadcasts the ADD_CLIENT command, 
		 * the client's Username, and the client's Zip Code to each advisor.
		 */
		private synchronized void addClient(Object[] client) {
			
			Object[] advisor;					// Holds an element of the connectedAdvisors vector
			ObjectOutputStream output;		// Advisor's ObjectOutputStream object located at advisor[0]
			
			chatroomClients.add(client);
			
			// For each element within the connectedAdvisor vector, write the username and zip code to the element's
			// ObjectOutputStream.
			for(int i = 0; i < chatroomAdvisors.size(); i++) {
				
				advisor = connectedAdvisors.elementAt(i);
				output = (ObjectOutputStream) advisor[0];
				
				try {
				
					output.writeInt(ChatroomConstants.ADD_CLIENT);	// Send command ADD_CLIENT
					output.writeUTF((String) client[1]);							// Write the Username
					output.writeUTF((String) client[2]);							// Write the Zip Code
					
				}
				catch(IOException exception) {
					
					System.out.printf("Error: Could not write to the advisor at index %d.\n", i);
					
				}
				
			}
				
			// Server Log
			System.out.printf("SERVER MESSAGE: Client: %s has logged into the server.\n", client[0]);
			
		}
		
		/* Function - addAdvisor(Object[] advisor)
		 * Adds the newly connected advisor to the chatroomAdvisors vector object.
		 */
		private synchronized void addAdvisor(Object[] advisor) {
			
			connectedAdvisors.add(advisor);
			
			// Server Log
			System.out.printf("SERVER MESSAGE: Advisor: %s has logged into the server.\n", advisor[1]);
			
		}
		
		/* Function - removeClient(Object[] client)
		 * Removes the client from the chatroomClients vector object and broadcasts the REMOVE_CLIENT command, 
		 * and the client's Username to each advisor.
		 */
		private synchronized void removeClient(Object[] client) {
			
			Object[] advisor;					// Holds an element of the connectedAdvisors vector
			ObjectOutputStream output;		// Advisor's ObjectOutputStream object located at advisor[0]
			
			connectedClients.remove(client);
			chatroomServer.removeClient(client);
			
			// For each element within the connectedAdvisor vector, write the username and zip code to the element's
			// ObjectOutputStream.
			for(int i = 0; i < chatroomAdvisors.size(); i++) {
				
				advisor = chatroomAdvisors.elementAt(i);
				output = (ObjectOutputStream) advisor[0];
				
				try {
				
					output.writeInt(ChatroomConstants.REMOVE_CLIENT);	// Send command ADD_CLIENT
					output.flush();
					
					output.writeUTF((String) advisor[1]);					// Write the Username
					output.flush();
					
				}
				catch(IOException exception) {
					
					System.out.printf("Error: Could not write to the advisor at index %d.\n", i);
					
				}
				
			}
			
			// Server Log
			System.out.printf("SERVER MESSAGE: Client: %s has logged off the server.\n", client[0]);
			
		}
		
		// Function - removeAdvisor(Object[] advisor)
		// Removes the connected advisor from the chatroomAdvisors vector object.
		private synchronized void removeAdvisor(Object[] advisor) {
			
			connectedAdvisors.remove(advisor);
			
			// Server Log
			System.out.printf("SERVER MESSAGE: Advisor: %s has logged off the server.\n", advisor[1]);
			
		}
		
		// Function - writeMessage(String message)
		// Broadcasts the WRITE_MESSAGE command and the message to each client and advisor within the chatroom
		private synchronized void writeMessage(String message) {
			
			Object[] advisor;
			Object[] client;
			ObjectOutputStream output;
			
			for(int i = 0; i < chatroomAdvisors.size(); i++) {
				
				advisor = chatroomAdvisors.elementAt(i);
				output = (ObjectOutputStream) advisor[0];
				
				try {
				
					output.writeInt(ChatroomConstants.WRITE_MESSAGE);
					output.flush();
					
					output.writeUTF(message);
					output.flush();
				
				}
				
				catch(IOException exception) {
					
					System.out.printf("Error: Could not write message to advisor %d.\n", advisor[1]);
					
				}
				
			}
			
			for(int i = 0; i < chatroomClients.size(); i++) {
				
				client = chatroomClients.elementAt(i);
				output = (ObjectOutputStream) client[0];
				
				try {
				
					output.writeInt(ChatroomConstants.WRITE_MESSAGE);
					output.flush();
				
					output.writeUTF(message);
					output.flush();
				
				}
				
				catch(IOException exception) {
					
					System.out.printf("Error: Could not write message to client %d.\n", client[1]);
					
				}
				
			}
			// Server Log
			System.out.printf("Message: %s\n", message);
		}
	
		private synchronized boolean isEmpty() {
			
			if(chatroomClients.size() == 0 && chatroomAdvisors.size() == 0)
				return true;
				
			return false;
			
		}
	}
	
	/* Class ServerSocketAgent
	 * Handles the interaction between the connected client/advisor and the server.
	*/
	class ServerSocketAgent implements Runnable{
		
		private ObjectOutputStream output;		// Object stream to a the server
		private ObjectInputStream	input;		// Object stream from a client/advisor

		/* Function - ServerSocketAgent(Socket socket)
		 * Default Constructor. Instantiate the ObjectOutputStream and ObjectInputStream. If the instantiation was
		 * successful, initiate this object's thread and begin runtime processing. If the instantiation failes, close the
		 * socket.
		 */
		public ServerSocketAgent(Socket socket){
		
			try{
			
				input = new ObjectInputStream(socket.getInputStream());
				output = new ObjectOutputStream(socket.getOutputStream());
				
				new Thread(this).start();

			}
			catch(IOException e){
			
				// If either instantiation failed, close the socket and return
				try {socket.close();}
				catch(IOException exception){}
				
			}
			
		}

		/* Function - run()
		 * Main Processing. Waits for a command from the client or advisor via the ObjectInputStream connected to the
		 * ServerSocketAgent.
		 * List of possible commands:
		 *		
		 *		CLIENT_CONNECTED
		 *			- Signals that the object connected to the socket is a Client
		 *				- Receives the client's Username and Zip Code
		 *			- Allocates a new array {ObjectOutputStream output, String Username, String Zip Code, int chatroom}
		 *			- Calls the addClient function of the ChatroomServer object passing the newly allocated array
		 *
		 *		ADVISOR_CONNECTED
		 *			- Signals that the object connected to the socket is an Advisor
		 *				- Receives the advisor's Username
		 *			- Allocates a new array {ObjectOutputStream output, String Username}
		 *			- Calls the addAdvisor function of the ChatroomServer objecy passing the newly allocated array
		 *
		 *		CLIENT_ROOM_CHANGE
		 *			- Signals a client change of room.
		 *				- Receives the client's userName
		 *				- Receives the chatroom number the client was moved to
		 *
		 *		CLIENT_JOINED_ROOM
		 *			- Signals that a client object has joined the room.
		 *
		 *		ADVISOR_JOINED_ROOM
		 *			- Signals that an advisor object has joined the room.
		 *
		 *		CLIENT_EXITED_ROOM
		 *			- Signals that a client object has exited the room.
		 *
		 *		ADVISOR_EXITED_ROOM
		 *			- Signals that an advisor object 
		 *
		 *		UPDATE_ADVISOR_STATUS
		 *			- Signals a change in the advisor status. The values can be either "" or "Advisor is typing..."
		 */
		public void run() {
								
			Object[] advisor;
			Object[] client;
			int command;					// Proposed action from the server
			String string;
			
			boolean isAdvisor = false;
			
			while(true){

				try {
				
					command = input.readInt();
					
					switch(command) {
						
						case ChatroomConstants.CLIENT_CONNECTED : 
						
							client = new Object[ChatroomConstants.CLIENT_COLUMN_COUNT + 1];
							
							client[0] = output;				// ObjectOutputStream
							client[1] = input.readUTF();	// Username
							client[2] = input.readUTF();	// ZipCode
							client[3] = "<Advisor>";
							
							chatroomServer.addClient(client);
							
							break;
									
						case ChatroomConstants.ADVISOR_CONNECTED : 
						
							advisor = new Object[ChatroomConstants.ADVISOR_COLUMN_COUNT];
							isAdvisor = true;
				
							advisor[0] = output;				// ObjectOutputStream
							advisor[1] = input.readUTF(); // Username
							
							chatroomServer.addAdvisor(advisor);
							
							break;
							
						case ChatroomConstants.CLIENT_DISCONNECTED :
							string = input.readUTF();
							chatroomServer.removeClient(getClient(string));
							break;
						case ChatroomConstants.GET_CLIENT_LIST :
							
							output.writeInt(connectedClients.size());
							output.flush();
							
							for(int i = 0; i < connectedClients.size(); i++) {
								
								client = connectedClients.elementAt(i);
									
								output.flush();
								
								output.writeUTF((String) client[1]);
								output.flush();
								
								output.writeUTF((String) client[2]);
								output.flush();
								
								output.writeUTF((String) client[3]);
								output.flush();
							}
						
					}
				} catch(Exception exception) {}
			}
		}
	}
}