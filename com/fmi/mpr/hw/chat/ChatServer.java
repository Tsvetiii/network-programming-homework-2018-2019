package com.fmi.mpr.hw.chat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

public class ChatServer {

	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 8888;

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		// Open a new DatagramSocket, which will be used to send the data.
		try (DatagramSocket serverSocket = new DatagramSocket()) {

			while (true) {

			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
