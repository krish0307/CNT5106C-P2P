package com.networks.p2p;

public class PeerProcess {
	public static void main(String[] args) {
		String peerIdInString = args[0];
		P2PLogger logger = P2PLogger.getInstance();
		logger.info(PeerProcess.class.getCanonicalName(), "main",
				"Logger initalized, PeerProcess has started successfully");
		PeerManager.getInstance(peerIdInString).initiateManager();
	}

}
