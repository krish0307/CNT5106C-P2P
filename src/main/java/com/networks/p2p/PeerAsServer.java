package com.networks.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class PeerAsServer implements Runnable {

	private String peerId;
	private PeerManager manager;

	private boolean serverCompleted = false;

	private static volatile PeerAsServer instance = null;

	public static PeerAsServer getInstance(String peerServerID, PeerManager manager) {
		if (instance == null) {
			instance = new PeerAsServer(peerServerID, manager);
		}
		return instance;
	}

	private PeerAsServer(String peerId, PeerManager manager) {
		this.peerId = peerId;
		this.manager = manager;
	}

	@Override
	public void run() {
		ServerSocket socket = null;
		try {
			HashMap<String, PeerData> peerInfoMap = FileParser.getInstance().getIdToPeerDataMap();
			PeerData serverPeerInfo = peerInfoMap.get(peerId);
			int peerServerPortNumber = serverPeerInfo.getPort();
			socket = new ServerSocket(peerServerPortNumber);
			long count = getNumberOfPeersToBeConnected();
//			logger.info(CLASS_NAME, "run", "Number of peer connections to be done are " + count);
			for (int i = 0; i < count; i++) {
				Socket conn = socket.accept();
//				logger.info(CLASS_NAME, "run", "Socket connection accepted");
				PeerHandler handler = PeerHandler.getInstance(conn, manager);
				manager.addHandler(handler);
				new Thread(handler).start();

			}
			setServerCompleted(true);
//			logger.info(CLASS_NAME, "run", "Server connections are completed");
		} catch (Exception e) {
//			logger.error(CLASS_NAME, "run", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
//				logger.error(CLASS_NAME, "run", e);
			}
		}
	}

	private long getNumberOfPeersToBeConnected() {
		return FileParser.getInstance().getIdToPeerDataMap().keySet().stream()
				.filter(id -> Integer.parseInt(id) > Integer.parseInt(peerId)).count();

	}

	public synchronized boolean isServerFinished() {
		return serverCompleted;
	}

	public synchronized void setServerCompleted(boolean isPeerServerCompleted) {
		this.serverCompleted = isPeerServerCompleted;
	}

}