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

	MulticastSocket clientSocket;
	InetAddress address;

	BufferedReader reader;

	Client(String inetAddr, int port) throws IOException {
		this.clientSocket = new MulticastSocket(port);
		address = InetAddress.getByName(inetAddr);

		clientSocket.joinGroup(address);

		reader = new BufferedReader(new InputStreamReader(System.in));
	}

	void sendMessage(String msg) throws IOException {
		DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, PORT);
		clientSocket.send(msgPacket);
	}

	void chat() throws IOException {
		Thread reading = new Thread(new ReadingThread(clientSocket, PORT, address));
		reading.start();

		while (true) {
			// System.out.print("Enter some text: ");
			String msg = reader.readLine();
			
			sendMessage(msg);
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		try {
			Client client = new Client(INET_ADDR, PORT);
			client.chat();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}