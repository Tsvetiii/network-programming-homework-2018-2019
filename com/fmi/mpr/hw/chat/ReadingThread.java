package com.fmi.mpr.hw.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ReadingThread implements Runnable {

	private String username;
	private MulticastSocket socket;

	ReadingThread(String username, MulticastSocket socket) {
		this.username = username;
		this.socket = socket;
	}

	@Override
	public void run() {
		byte[] buf;

		while (Client.isLoggedIn) {
			buf = new byte[Client.BUFF_SIZE];

			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(msgPacket);
				String msg = new String(buf, 0, buf.length);

				String msgType = msg.split(" ")[0].trim();
				String msgData = msg.substring(msgType.length()).trim();

				if (msgData.startsWith(username)) {
					continue;
				}

				switch (msgType) {
				case "TEXT": {
					System.out.println(msgData);
					break;
				}
				case "IMAGE": {
					System.out.println(msgData);
					String[] splitted = msgData.split(" ");
					String imageName = splitted[splitted.length - 1];
					buf = new byte[Client.BUFF_SIZE];
					msgPacket = new DatagramPacket(buf, buf.length);
					int bytesWritten;
					try (FileOutputStream out = new FileOutputStream(new File(imageName))) {
						do {
							socket.receive(msgPacket);
							out.write(msgPacket.getData(), msgPacket.getOffset(), msgPacket.getLength());
							out.flush();
							bytesWritten = msgPacket.getData().length;
						} while (msgPacket != null && bytesWritten > 0 && bytesWritten <= Client.BUFF_SIZE);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				default:
					System.out.println("The packet is here. And is: ");
					System.out.println(msgData);
					break;
				}

			} catch (SocketException e) {
				System.out.println("You logged out.");
			} catch (SocketTimeoutException e) {
				System.out.println("Timeout");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
