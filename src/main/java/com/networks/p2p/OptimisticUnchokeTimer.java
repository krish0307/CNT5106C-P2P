package com.networks.p2p;


import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;

/**
 * OptimisticUnchokeManager
 */
public class OptimisticUnchokeTimer extends TimerTask {
	private static OptimisticUnchokeTimer instance = null;
	private PeerManager controller = null;
	private P2PLogger logger = null;

	/**
	 * get instance
	 * @param controller
	 * @return
	 */
	public static synchronized OptimisticUnchokeTimer getInstance(PeerManager controller) {
		if (instance == null) {
			if (controller == null) {
				return null;
			}
			instance = new OptimisticUnchokeTimer();
			instance.controller = controller;
			instance.logger = controller.getLogger();
		}
		return instance;
	}

	public void destroy() {
		this.cancel();
	}

	/**
	 * run
	 */
	public void run() {
		ArrayList<String> chokedPeers = controller.getChokedPeers();
		if (chokedPeers.size() > 0) {
			Random random = new Random();
			controller.optimisticallyUnChkPeers(chokedPeers.get(random.nextInt(chokedPeers.size())));
		}

		controller.fileDownloadComplete();
		if (controller.isDownloadComplete()) {
//			logger.info("Peer [" + controller.getPeerId() + "] has downloaded the complete file.");
			controller.sendShutdown();
		}
	}


}
