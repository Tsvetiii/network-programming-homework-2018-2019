package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class Client {

	final static int BUFF_SIZE = 65554;
	final static String USERS_FILE = "users.txt";
	final static String TEMP_FILE = "temp.txt";
	final static String LOGOUT = "LOGOUT";
	final static String WELCOME_MSG = "Welcome to the chat room. \nEvery message you want to send must start with the message type"
			+ " (TEXT, VIDEO, IMAGE), \nfollowed by one space and then the message itself. \n"
			+ "If you want to leave the chat room, you must enter LOGOUT. Enjoy!";

	static volatile boolean isLoggedIn = false;

	static Scanner consoleReader = new Scanner(System.in);

	String username;
	MulticastSocket clientSocket;
	InetAddress address;
	int port;

	BufferedReader reader;

	Client(String username, String inetAddr, int port) throws IOException {
		this.username = username;
		this.clientSocket = new MulticastSocket(port);
		this.address = InetAddress.getByName(inetAddr);
		this.port = port;

		clientSocket.joinGroup(address);
		isLoggedIn = true;

		this.reader = new BufferedReader(new InputStreamReader(System.in));
	}

	void sendMessage(String msg) throws IOException {
		String msgType = msg.split(" ")[0].trim().toUpperCase();
		String msgData = msg.substring(msgType.length()).trim();

		String message = null;

		switch (msgType) {
		case "TEXT": {
			message = new StringBuilder().append("TEXT ").append(username).append(": ").append(msgData).toString();
			DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
			clientSocket.send(msgPacket);
			break;
		}
		case "VIDEO":

		case "IMAGE": {
			File file = new File(msgData);
			if (!file.exists()) {
				System.out.println("No such file.");
				break;
			}
			String imageName = file.getName();
			String info = (msgType.equals("IMAGE")) ? " sent image " : " sent video ";
			message = new StringBuilder().append("IMAGE ").append(username).append(info).append(imageName).toString();
			DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
			clientSocket.send(msgPacket);

			try (FileInputStream in = new FileInputStream(new File(msgData))) {
				byte[] buff = new byte[BUFF_SIZE];
				int readBytes;
				while ((readBytes = in.read(buff, 0, BUFF_SIZE)) > 0) {
					msgPacket = new DatagramPacket(buff, readBytes, address, port);
					clientSocket.send(msgPacket);
				}
			} catch (IOException e) {
				System.out.println("Could not send the message.");
				e.printStackTrace();
			}
			break;
		}

		case "LOGOUT": {
			DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);
			clientSocket.send(msgPacket);
			logout();
			break;
		}

		default:
			System.out.println("Invalid message type!");
			break;
		}
	}

	void chat() throws IOException {
		Thread reading = new Thread(new ReadingThread(username, clientSocket));
		reading.start();

		System.out.println(WELCOME_MSG);

		while (isLoggedIn) {
			String msg = reader.readLine();
			if (msg.toUpperCase().equals(LOGOUT)) {
				sendMessage("LOGOUT " + username + " logged out.");
			} else {
				sendMessage(msg);
			}
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

	private static String login() {
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
		} catch (IOException e) {
			System.out.println("Error during login.");
			System.exit(0);
		}
		return username;
	}

	private void logout() throws IOException {
		try {
			removeFromActiveUsers();
			isLoggedIn = false;
			clientSocket.leaveGroup(address);
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("Error during logout.");
		} finally {
			closeStreams();
		}
	}

	private void closeStreams() throws IOException {
		consoleReader.close();
		reader.close();
	}

	private synchronized void removeFromActiveUsers() throws IOException {
		String tempFilename = TEMP_FILE;
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

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Expected: InetAddress and Port.");
			return;
		}

		String inetAddress = null;
		int port = 0;
		try {
			inetAddress = args[0];
			port = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Invalid address or port.");
			return;
		}

		String user = login();
		Client client = null;
		try {
			client = new Client(user, inetAddress, port);
			client.chat();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}