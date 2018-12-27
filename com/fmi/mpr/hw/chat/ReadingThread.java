package com.fmi.mpr.hw.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReadingThread implements Runnable {

	private MulticastSocket socket;
	private InetAddress group;
	private int port;

	ReadingThread(MulticastSocket socket, int port, InetAddress group) {
		this.socket = socket;
		this.group = group;
		this.port = port;
	}

	@Override
	public void run() {
		byte[] buf;

		while (true) {
			buf = new byte[256];

			// receiving information
			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(msgPacket);
				// Need to filter the messages from the same user
				String msg = new String(buf, 0, buf.length);
				System.out.println("Received: " + msg);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
