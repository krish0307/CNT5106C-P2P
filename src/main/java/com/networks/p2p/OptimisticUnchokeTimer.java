package com.networks.p2p;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;

public class OptimisticUnchokeTimer extends TimerTask {
	private static OptimisticUnchokeTimer instance = null;
	private PeerManager manager = null;
	private P2PLogger logger = null;

	private OptimisticUnchokeTimer(PeerManager manager) {
		this.manager = manager;
		this.logger = manager.getLogger();
	}

	public static synchronized OptimisticUnchokeTimer getInstance(PeerManager manager) {
		if (instance == null) {
			instance = new OptimisticUnchokeTimer(manager);

		}
		return instance;
	}

	public void kill() {
		this.cancel();
	}

	public void run() {
		ArrayList<String> chkPeers = manager.getChokedPeers();
		if (chkPeers.size() > 0) {
			Random random = new Random();
			manager.optimisticallyUnChkPeers(chkPeers.get(random.nextInt(chkPeers.size())));
		}

		manager.fileDownloadComplete();
		if (manager.isDownloadComplete()) {
			manager.sendShutdown();
		}
	}

}
