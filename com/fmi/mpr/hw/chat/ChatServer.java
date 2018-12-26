package com.fmi.mpr.hw.chat;

//Java implementation of Server side 
//It contains two classes : Server and ClientHandler 
//Save file as Server.java 

import java.io.*;
import java.util.*;
import java.net.*;

//Server class 
public class ChatServer {

	// Vector to store active clients
	static Vector<ClientHandler> ar = new Vector<>();

	// counter for clients
	static int i = 0;

	public static void main(String[] args) throws IOException {
		// server is listening on port 1234
		ServerSocket ss = new ServerSocket(1234);

		Socket s;

		// running infinite loop for getting
		// client request
		while (true) {
			// Accept the incoming request
			s = ss.accept();

			System.out.println("New user request received : " + s);

			// obtain input and output streams
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());

			System.out.println("Creating a new handler for this user...");

			// Create a new handler object for handling this request.
			ClientHandler mtch = new ClientHandler(s, "user " + i, dis, dos);

			// Create a new Thread with this object.
			Thread t = new Thread(mtch);

			System.out.println("Adding this user to active user list");

			// add this client to active clients list
			ar.add(mtch);

			// start the thread.
			t.start();

			//ar.remove(mtch);

			// increment i for new client.
			// i is used for naming only, and can be replaced
			// by any naming scheme
			i++;
		}
	}
}
