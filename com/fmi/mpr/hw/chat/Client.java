package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 8888;
	final static String USERS_FILE = "users.txt";

	static volatile boolean isLoggedIn = false;

	static Scanner consoleReader = new Scanner(System.in);

	String username;
	MulticastSocket clientSocket;
	InetAddress address;

	BufferedReader reader;

	Client(String username, String inetAddr, int port) throws IOException {
		this.username = username;
		this.clientSocket = new MulticastSocket(port);
		this.address = InetAddress.getByName(inetAddr);

		clientSocket.joinGroup(address);
		isLoggedIn = true;

		this.reader = new BufferedReader(new InputStreamReader(System.in));
	}

	void sendMessage(String msg) throws IOException {
		DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, PORT);
		clientSocket.send(msgPacket);
	}

	void chat() throws IOException {
		Thread reading = new Thread(new ReadingThread(username, clientSocket, PORT, address));
		reading.start();

		while (isLoggedIn) {
			System.out.print("Enter some text: ");
			String msg = reader.readLine();
			if (msg.equals("logout")) {
				logout();
				break;
			}
			sendMessage(username + ": " + msg);
		}
	}

	private static boolean isAlreadyLoggedIn(String username) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
			String currentUser;

			while ((currentUser = reader.readLine()) != null) {
				if (currentUser.equals(username)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String login() throws IOException {
		String username = "";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
			System.out.print("Enter username: ");
			username = consoleReader.nextLine();
			while (username.isEmpty() || isAlreadyLoggedIn(username)) {
				System.out.println("There is already a user with that name.");
				System.out.print("Enter username: ");
				username = consoleReader.nextLine();
			}
			System.out.println("You are logged in as " + username);
			synchronized (writer) {
				writer.write(username);
				writer.newLine();
			}
		}
		return username;
	}

	private void logout() throws IOException {
		sendMessage(username + " logged out.");
		removeFromActiveUsers();
		isLoggedIn = false;
		clientSocket.close();
	}

	private synchronized void removeFromActiveUsers() throws IOException {
		String tempFilename = "temp.txt";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename));
				BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {

			String currentUser;

			while ((currentUser = reader.readLine()) != null) {
				if (currentUser.equals(username)) {
					continue;
				}
				writer.write(currentUser);
				writer.newLine();
			}
		}

		new File(USERS_FILE).delete();
		new File(tempFilename).renameTo(new File(USERS_FILE));
	}

	// Should change this
	protected void finalize() {
		consoleReader.close();
		// new File(USERS_FILE).delete();
	}

	public static void main(String[] args) throws UnknownHostException {
		try {
			String user = login();
			Client client = new Client(user, INET_ADDR, PORT);
			client.chat();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			new File(USERS_FILE).delete();
		}

	}
}