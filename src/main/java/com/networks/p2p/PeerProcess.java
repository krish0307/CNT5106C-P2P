package com.networks.p2p;
public class PeerProcess {
	public static void main(String args[]){
		String peerID = args[0];
		
		PeerManager peerManager = PeerManager.getInstance(peerID);
		peerManager.initiateManager();
		
	}
	
}
