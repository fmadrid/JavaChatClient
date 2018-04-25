// Author: 	Frank Madrid
// Purpose:	CMPS 394 Homework #8 Application of Multithreading, Synchronization, Socket and OOP
//											Design and Implementation
//					- Get familiar with and apply following concepts to build appliations
//							- Apply Multithreading, Thread-Synchronization, Network Programming to solve more complicated problems
//							- Use OOP, top-down design method in program design and implementation.

interface ChatroomConstants {

	// Client Window Parameters
	final int PROMPT_WINDOW_FRAME_LENGTH = 500;
	final int PROMPT_WINDOW_FRAME_HEIGHT = 100;
	
	final int CHATROOM_WINDOW_FRAME_LENGTH = 750;
	final int CHATROOM_WINDOW_FRAME_HEIGHT = 300;

	// Client Properties
	final int CLIENT_COLUMN_COUNT = 3;
	final int ADVISOR_COLUMN_COUNT = 2;
	
	// Commands sent and received by the server and client
	final int CLIENT_CONNECTED = 10;
	final int ADVISOR_CONNECTED = 11;
	
	final int CLIENT_DISCONNECTED = 20;
	final int ADVISOR_DISCONNECTED = 21;
	
	final int CLIENT_ROOM_CHANGE = 22;
	
	final int CLIENT_JOINED_ROOM = 30;
	final int ADVISOR_JOINED_ROOM = 31;
	
	final int CLIENT_EXITED_ROOM = 40;
	final int ADVISOR_EXITED_ROOM = 41;
	
	final int ROOM_CHANGE = 50;
	final int UPDATE_ADVISOR_STATUS = 51;
	
	final int WRITE_MESSAGE = 60;
	final int ADD_CLIENT = 61;
	final int REMOVE_CLIENT = 62;
	
	final int GET_CLIENT_LIST = 71;
	
}