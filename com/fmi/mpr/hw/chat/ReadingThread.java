package com.fmi.mpr.hw.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class ReadingThread implements Runnable {

	private String username;
	private MulticastSocket socket;
	private InetAddress address;
	private int port;

	ReadingThread(String username, MulticastSocket socket, int port, InetAddress address) {
		this.username = username;
		this.socket = socket;
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		byte[] buf;

		while (Client.isLoggedIn) {
			buf = new byte[256];

			// receiving information
			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(msgPacket);
				String msg = new String(buf, 0, buf.length);
				if (msg.startsWith(username)) {
					continue;
				}
				System.out.println(msg);

			} catch (SocketException e) {
				System.out.println("You logged out.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
