package com.networks.p2p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;

/**
 * ChokeUnchokeManager
 */
@SuppressWarnings("unchecked")
public class ChokeUnchokeTimer extends TimerTask {

	private PeerManager controller = null;
	private P2PLogger logger = null;

	private static volatile ChokeUnchokeTimer instance = null; // static instance

	/* task */

	/**
	 * get instance
	 * 
	 * @param controller
	 * @return
	 */
	public static synchronized ChokeUnchokeTimer getInstance(PeerManager controller) {
		if (instance == null) {
			if (controller == null) {
				return null;
			}

			instance = new ChokeUnchokeTimer();
			instance.logger = controller.getLogger();
			instance.controller = controller;
		}

		return instance;
	}

	public void destroy() {
		// System.out.println(LOGGER_PREFIX + " Shutting down
		// ChokeUnchokeManager......");
		this.cancel();
	}

	public void run() {

		int preferredNeighbors = 0;
		preferredNeighbors = (FileParser.getInstance().getCommonFileData().getNumberOfPreferredNeighbors());
		HashMap<String, Double> speedMap = controller.getSpeed();
		if (speedMap.size() >= preferredNeighbors) {
			ArrayList<String> chokedPeerList = new ArrayList<String>();
			ArrayList<String> unchokePeers = new ArrayList<String>();

			Set<Entry<String, Double>> entrySet = speedMap.entrySet();
			List<Entry<String, Double>> mainList = new ArrayList<>();
			mainList.addAll(entrySet);

			Collections.sort(mainList, new Comparator<Entry<String, Double>>() {
				public int compare(Entry<String, Double> t1, Entry<String, Double> t2) {
					return t1.getValue().compareTo(t2.getValue());
				}
			});

			for (int i = 0; i < preferredNeighbors; i++) {
				unchokePeers.add(mainList.get(i).getKey());
			}
			for (int i = preferredNeighbors; i < mainList.size(); i++) {
				chokedPeerList.add(mainList.get(i).getKey());
			}
			try {
				controller.unChkPeers(unchokePeers);
				controller.setChokePeers(chokedPeerList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
