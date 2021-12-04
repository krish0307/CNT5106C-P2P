package com.networks.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * PeerServer
 */
public class PeerAsServer implements Runnable {

	private String peerServerID; // peer server id
	private PeerManager controller;

	private boolean serverCompleted = false;

	private static volatile PeerAsServer instance = null;

	/**
	 * getInstance
	 * 
	 * @param peerServerID
	 * @param controller
	 * @return
	 */
	public static PeerAsServer getInstance(String peerServerID, PeerManager controller) {
		if (instance == null) {
			instance = new PeerAsServer();
			instance.peerServerID = peerServerID;
			instance.controller = controller;
		}
		return instance;
	}


	@Override
	public void run() {
		ServerSocket socket = null;
		try {
			HashMap<String, PeerData> peerInfoMap = FileParser.getInstance().getIdToPeerDataMap();
			PeerData serverPeerInfo = peerInfoMap.get(peerServerID);
			int peerServerPortNumber = serverPeerInfo.getPort();
			socket = new ServerSocket(peerServerPortNumber);
			long count = getNumberOfPeersToBeConnected();
//			logger.info(CLASS_NAME, "run", "Number of peer connections to be done are " + count);
			for (int i = 0; i < count; i++) {
				Socket conn = socket.accept();
//				logger.info(CLASS_NAME, "run", "Socket connection accepted");
//				PeerHandler handler = new PeerHandler(conn, manager);
				PeerHandler handler = PeerHandler.getNewInstance(conn, controller);
//				handler.setPeerId(peerId);
				controller.addHandler(handler);
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
				.filter(id -> Integer.parseInt(id) > Integer.parseInt(peerServerID)).count();

	}

	public synchronized boolean isServerFinished() {
		return serverCompleted;
	}

	public synchronized void setServerCompleted(boolean isPeerServerCompleted) {
		this.serverCompleted = isPeerServerCompleted;
	}

}