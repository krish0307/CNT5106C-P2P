package com.networks.p2p;

import java.util.List;

public class StartRemotePeers {

	public static void main(String[] args) {

		List<PeerData> peersData = FileParser.getInstance().parsePeersFile(Constants.PEER_FILE_NAME);
		String path = System.getProperty("user.dir");
		try {
			for (int i = 0; i < peersData.size(); i++) {
				PeerData data = (PeerData) peersData.get(i);
				System.out.println("Starting remote peer " + data.getPeerId() + " at " + data.getPeerAddress());
				Runtime.getRuntime()
						.exec("ssh " + data.getPeerAddress() + " cd " + path + "; java PeerProcess " + data.getPeerId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}