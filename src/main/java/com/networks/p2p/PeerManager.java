package com.networks.p2p;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PeerManager {

	private static PeerManager instance;
	private String peerId;
	private List<PeerHandler> handlerList;

	private PeerManager(String peerId) {
		this.peerId = peerId;
		handlerList = new ArrayList<>();
	}

	public static PeerManager getInstance(String peerId) {
		if (instance == null) {
			instance = new PeerManager(peerId);
		}
		return instance;
	}

	public void initiateManager() {
		List<PeerData> peersData = FileParser.getInstance().parsePeersFile(Constants.PEER_FILE_NAME);
//		CommonData commonData = FileParser.getInstance().parseCommonFile(Constants.COMMON_FILE_NAME);
//		List<PeerData> neighbourPeers = FileParser.getInstance().getNeighbhourPeersData(peerId);
		createServerForThisPeer(peersData);

		connectToExistingPeers();
		
	}

	private void createServerForThisPeer(List<PeerData> peersData) {
		PeerData myData = peersData.stream().filter(data -> data.getPeerId().equals(peerId)).findAny().get();
		PeerAsServer server = new PeerAsServer(peerId, myData.getPeerPort(),this);
		new Thread(server).start();
	}

	private void connectToExistingPeers() {
		try {
			List<PeerData> numberOfConnectedPeersInSystem = getNumberOfConnectedPeersInSystem();
			for (PeerData connectedNeighbours : numberOfConnectedPeersInSystem) {
				int peerPort = Integer.parseInt(connectedNeighbours.getPeerPort());
				String peerAddress = connectedNeighbours.getPeerAddress();
				Socket neighbourSocket = new Socket(peerAddress, peerPort);
				PeerHandler handler = new PeerHandler(neighbourSocket,connectedNeighbours.getPeerId(),this);
				new Thread(handler).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<PeerData> getNumberOfConnectedPeersInSystem() {
		return FileParser.getInstance().getIdToPeerDataMap().values().stream()
				.filter(data -> Integer.parseInt(data.getPeerId()) < Integer.parseInt(peerId))
				.collect(Collectors.toList());

	}

	public void addHandler(PeerHandler handler) {
		handlerList.add(handler);
	}
}
