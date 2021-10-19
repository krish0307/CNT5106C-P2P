package com.networks.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerAsServer implements Runnable {
	private int peerPort;
	private String peerId;

	public PeerAsServer(String peerId, String peerPort) {
		this.peerPort = Integer.parseInt(peerPort);
	}

	@Override
	public void run() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(peerPort);
			long count = getNumberOfPeersToBeConnected();
			for (int i = 0; i < count; i++) {
				Socket conn = socket.accept();
				PeerHandler handler = new PeerHandler(conn, peerId);
				new Thread(handler).start();

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private long getNumberOfPeersToBeConnected() {
		return FileParser.getInstance().getIdToPeerDataMap().keySet().stream()
				.filter(id -> Integer.parseInt(id) > Integer.parseInt(peerId)).count();

	}

}
