package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Client {

	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 8888;

	public static void main(String[] args) throws UnknownHostException {
		// Get the address that we are going to connect to.
		InetAddress address = InetAddress.getByName(INET_ADDR);

		BufferedReader consoleReader;

		// Create a new Multicast socket (that will allow other sockets/programs
		// to join it as well.
		try (MulticastSocket clientSocket = new MulticastSocket(PORT)) {
			// Joint the Multicast group.
			clientSocket.joinGroup(address);
			consoleReader = new BufferedReader(new InputStreamReader(System.in));

			Thread reading = new Thread(new ReadingThread(clientSocket, PORT, address));
			reading.start();

			while (true) {
				// System.out.print("Enter some text: ");
				String msg = consoleReader.readLine();

				DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, PORT);
				clientSocket.send(msgPacket);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}