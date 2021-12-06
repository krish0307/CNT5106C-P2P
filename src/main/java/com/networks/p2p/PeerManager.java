package com.networks.p2p;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class PeerManager {

	private ArrayList<PeerHandler> handlersList;
	private BitsManager bitsManager;
	private static final String CLASS_NAME = PeerManager.class.getCanonicalName();

	private HashMap<String, String> downloadedPeers = new HashMap<String, String>();
	private ArrayList<String> chokedPeers = new ArrayList<String>();
	private ChokeUnchokeTimer chokeUnchokeTimer;
	private OptimisticUnchokeTimer optimisticUnchokeTimer;
	private PeerAsServer peerAsServer;
	private String peerId;
	private P2PLogger logger;
	private boolean connectionDone = false;

	private static volatile PeerManager instance = null;

	public static synchronized PeerManager getInstance(String peerID) {
		if (instance == null) {
			instance = new PeerManager(peerID);
		}
		return instance;
	}

	private PeerManager(String peerId) {
		this.peerId = peerId;
		handlersList = new ArrayList<>();
		logger = P2PLogger.getInstance(peerId);
		FileParser.getInstance().parsePeersFile(Constants.PEER_INFO_FILE);

		boolean fileStatus = FileParser.getInstance().getIdToPeerDataMap().get(peerId).isFileExist();
		bitsManager = BitsManager.getInstance(fileStatus, peerId);
		downloadedPeers = new HashMap<>();

	}

	public void initiateManager() {
		logger.info(CLASS_NAME, "initiateManager", "Logger initalized");
		peerAsServer = PeerAsServer.getInstance(peerId, this);
		new Thread(peerAsServer).start();

		connectToPreviousPeer();

		CommonData commonFileData = FileParser.getInstance().getCommonFileData();

		chokeUnchokeTimer = ChokeUnchokeTimer.getInstance(this);
		new Timer().schedule(chokeUnchokeTimer, 0, commonFileData.getUnchokingInterval() * 1000);

		optimisticUnchokeTimer = OptimisticUnchokeTimer.getInstance(this);

		new Timer().schedule(optimisticUnchokeTimer, 0, commonFileData.getOptimisticUnchokingInterval() * 1000);
	}


	private void connectToPreviousPeer() {
		try {
			logger.info(CLASS_NAME, "connectToPreviousPeer", "Connecting to previous peers");
			List<PeerData> numberOfConnectedPeersInSystem = getNumberOfConnectedPeersInSystem();
			for (PeerData connectedNeighbours : numberOfConnectedPeersInSystem) {
				int peerPort = (connectedNeighbours.getPort());
				String peerAddress = connectedNeighbours.getAddress();
				Socket neighbourSocket = new Socket(peerAddress, peerPort);
				PeerHandler handler = PeerHandler.getInstance(neighbourSocket, this);
				handler.setPeerId(connectedNeighbours.getPeerId());
				addHandler(handler);
				new Thread(handler).start();
			}
			setAllPeersConnection(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<PeerData> getNumberOfConnectedPeersInSystem() {
		return FileParser.getInstance().getIdToPeerDataMap().values().stream()
				.filter(data -> Integer.parseInt(data.getPeerId()) < Integer.parseInt(peerId))
				.collect(Collectors.toList());

	}

	public void fileDownloadComplete() {
		if (!isConnection() || !peerAsServer.isServerFinished()) {
			return;
		}
		if (FileParser.getInstance().getIdToPeerDataMap().size() == downloadedPeers.size()) {
			shutdown();
		}
	}

	public void shutdown() {
		chokeUnchokeTimer.kill();
		optimisticUnchokeTimer.kill();
		bitsManager.close();
		System.exit(0);
	}

	public synchronized void addHandler(PeerHandler peerHandler) {
		handlersList.add(peerHandler);
	}

	public synchronized Message getBitFieldMessage() {
		Message message = Message.create();
		message.setMessageType(MessageType.BITFIELD);
		message.setBitField(bitsManager.getBitField());

		return message;
	}

	public HashMap<String, Double> getSpeed() {
		HashMap<String, Double> speedList = new HashMap<>();
		for (int i = 0; i < handlersList.size(); i++) {
			PeerHandler peerHandler = handlersList.get(i);
			speedList.put(peerHandler.getPeerId(), peerHandler.getSpeed());
		}
		return speedList;
	}

	public void setChokePeers(ArrayList<String> peerList) {
		chokedPeers = peerList;

		Message chokeMessage = Message.create();
		chokeMessage.setMessageType(MessageType.CHOKE);

		for (int i = 0; i < peerList.size(); i++) {
			String peerIdTmp = peerList.get(i);
			for (int j = 0, peerHandlersSize = handlersList.size(); j < peerHandlersSize; j++) {
				PeerHandler peerHandler = handlersList.get(j);
				if (peerHandler.getPeerId().equals(peerIdTmp)) {
					if (peerHandler.isHandshakeRcvd()) {
						peerHandler.sendChokeMessage(chokeMessage);
						break;
					} else {
						break;
					}
				}
			}
		}
	}


	public void unChkPeers(ArrayList<String> peerList) {
		Message unChokeMessage = Message.create();
		unChokeMessage.setMessageType(MessageType.UNCHOKE);
		// System.out.println(LOGGER_PREFIX+" : Sending UNCHOKE message to peers...");
		for (int i = 0; i < peerList.size(); i++) {
			String peerToBeUnChoked = peerList.get(i);
			for (int j = 0; j < handlersList.size(); j++) {
				PeerHandler peerHandler = handlersList.get(j);
				if (peerHandler.getPeerId().equals(peerToBeUnChoked)) {
					if (peerHandler.isHandshakeRcvd()) {
						// System.out.println(LOGGER_PREFIX+" : Sending UNCHOKE message to
						// peers..."+peerToBeUnChoked);
						peerHandler.sendUnchokeMessage(unChokeMessage);
						break;
					} else {
						break;
					}
				}
			}
		}
	}


	public void optimisticallyUnChkPeers(String peerToBeUnChoked) {
		Message unChokeMessage = Message.create();
		unChokeMessage.setMessageType(MessageType.UNCHOKE);

//		logger.info("Peer [" + peerId + "] has the optimistically unchoked neighbor [" + peerToBeUnChoked + "]");
		for (int i = 0, peerHandlersSize = handlersList.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = handlersList.get(i);
			if (!peerHandler.getPeerId().equals(peerToBeUnChoked)) {
				continue;
			}
			if (peerHandler.isHandshakeRcvd()) {
				peerHandler.sendUnchokeMessage(unChokeMessage);
				break;
			} else {
				break;
			}
		}
	}


	public synchronized void insertData(Message pieceMessage, String sourcePeerID) {
		bitsManager.writeData(pieceMessage.getIndex(), pieceMessage.getData());
//		logger.info("Peer [" + instance.getPeerId() + "] has downloaded the piece [" + pieceMessage.getIndex() + "] from [" + sourcePeerID + "]. Now the number of pieces it has is " + (pieceManager.getBitField().getNoOfPieces()));
	}


	public Message genBitMessage(int index) {
		Data piece = bitsManager.get(index);
		if (piece != null) {
			Message message = Message.create();
			message.setData(piece);
			message.setIndex(index);
			message.setMessageType(MessageType.PIECE);
			return message;
		}
		return null;
	}


	public void sendHaveMessage(int pieceIndex, String fromPeerID) {
		Message haveMessage = Message.create();
		haveMessage.setIndex(pieceIndex);
		haveMessage.setMessageType(MessageType.HAVE);

		for (int i = 0, peerHandlersSize = handlersList.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = handlersList.get(i);
			// System.out.println(LOGGER_PREFIX+": Sending have message from "+peerID+" to :
			// "+peerHandler.getPeerId());
			if (fromPeerID.equals(peerHandler.getPeerId())) {
				continue;
			}
			peerHandler.sendHaveMessage(haveMessage);
		}
	}


	public void sendShutdown() {
		if (!isConnection() || !peerAsServer.isServerFinished()) {
			return;
		}
		Message shutdownMessage = Message.create();
		shutdownMessage.setMessageType(MessageType.SHUTDOWN);

		setFileDownloadComplete(peerId);
		for (int i = 0, peerHandlersSize = handlersList.size(); i < peerHandlersSize; i++) {
			PeerHandler peerHandler = handlersList.get(i);
			peerHandler.sendShutdownMessage(shutdownMessage);
		}
	}

	public synchronized void setFileDownloadComplete(String peer) {
		downloadedPeers.put(peer, " ");
	}

	public String getPeerId() {
		return peerId;
	}

	public void setAllPeersConnection(boolean isAllPeersConnection) {
		this.connectionDone = isAllPeersConnection;
	}

	public ArrayList<String> getChokedPeers() {
		return chokedPeers;
	}

	public synchronized P2PLogger getLogger() {
		return logger;
	}

	public boolean isConnection() {
		return connectionDone;
	}

	public boolean isDownloadComplete() {
		return bitsManager.isDownloadComplete();
	}

}
