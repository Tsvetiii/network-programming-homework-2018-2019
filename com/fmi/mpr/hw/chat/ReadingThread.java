package com.fmi.mpr.hw.chat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.sun.prism.Image;

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
			buf = new byte[1024];

			// receiving information
			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(msgPacket);
				String msg = new String(buf, 0, buf.length);

				String msgType = msg.split(" ")[0].trim();
				String msgData = msg.substring(msgType.length()).trim();

				switch (msgType) {
				case "TEXT": {
					if (msgData.startsWith(username)) {
						continue;
					}
					System.out.println(msgData);
					break;
				}
				case "IMAGE": {
//					BufferedImage img = ImageIO.read(new ByteArrayInputStream(msgData.getBytes()));
//					ImageIO.write(img, "jpg", new File());
//					// ImageIcon set = new ImageIcon(img);
//					// Image.setIcon(set);
					break;
				}
				default:
					break;
				}

			} catch (SocketException e) {
				System.out.println("You logged out.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
