package com.networks.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerHandler implements Runnable {
	private static final String CLASS_NAME = PeerHandler.class.getCanonicalName();
	private PeerManager manager;
	private ObjectInputStream inputStream;
	private PeerMessanger peerMesssanger;
	private RequestProcessor requestProcessor;
	private P2PLogger logger;

	private String peerId;
	private Socket otherSocket;

	private boolean lastMessageReceived = true;
	private boolean isChokedNghbrPeer = false;
	private boolean isHandshakeRcvd = false;
	private boolean isHandShakeSent = false;
	private boolean isChoked = false;

	private long downloadStartTime = 0;
	private int dataSize = 0;

	synchronized public static PeerHandler getNewInstance(Socket socket, PeerManager controller) {
		PeerHandler peerHandler = new PeerHandler(socket, controller);
		return peerHandler;
	}

	private PeerHandler(Socket socket, PeerManager manager) {
		this.otherSocket = socket;
		this.manager = manager;
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(otherSocket.getOutputStream());
			inputStream = new ObjectInputStream(otherSocket.getInputStream());

			if (manager == null) {
				close();
			}

			peerMesssanger = PeerMessanger.getInstance(outputStream);
			if (peerMesssanger == null) {
				close();
			}
			new Thread(peerMesssanger).start();

			requestProcessor = RequestProcessor.getInstance(manager, this);
			new Thread(requestProcessor).start();

			logger = manager.getLogger();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized public void close() {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException ignore) {
		}
	}

	public void run() {

		if (peerId != null) {
			sendHandShakeMessage();
		}
		try {
			while (true) {

				IMessage message = (IMessage) inputStream.readObject();

				switch (message.getType()) {
				case HANDSHAKE:
					if (message instanceof HandshakeMessage) {
						HandshakeMessage handshakeMessage = (HandshakeMessage) message;
						processHandShakeMessage(handshakeMessage);
					}
					break;
				case REQUEST: {
					processRequestMessage((Message) message);
					break;
				}
				case BITFIELD:
					processBitFieldMessage((Message) message);
					break;
				case CHOKE: {
					processChokeMessage((Message) message);
					break;
				}
				case HAVE: {
					processHaveMessage((Message) message);
					break;
				}
				case INTERESTED: {
					processInterestedMessage((Message) message);
					break;
				}
				case NOTINTERESTED: {
					processNotInterestedMessage((Message) message);
					break;
				}
				case PIECE: {
					processPieceMessage((Message) message);
					break;
				}
				case UNCHOKE: {
					processUnchockMessage((Message) message);
					break;
				}
				case SHUTDOWN:
					handleShutDownMessage((Message) message);
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			logger.error(CLASS_NAME, "run", e);
		}
	}

	private void processUnchockMessage(Message unchokeMessage) {
		logger.info(CLASS_NAME, "processUnchockMessage", "peer is unchoked by " + peerId);
		isChokedNghbrPeer = false;
		try {
			requestProcessor.addMessage(unchokeMessage);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "processUnchockMessage", e);
		}
	}

	private void processPieceMessage(Message messge) {
		logger.info(CLASS_NAME, "processPieceMessage", "Processing piece message from peer " + peerId);
		manager.insertData(messge, peerId);
		manager.sendHaveMessage(messge.getIndex(), peerId);
		dataSize += messge.getData().getSize();
		setPreviousMessageRcvd(true);
		try {
			requestProcessor.addMessage(messge);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "processPieceMessage", e);
		}
	}

	private void processChokeMessage(Message message) {
		logger.info(CLASS_NAME, "processChokeMessage", "Peer  is choked by " + peerId);
		isChokedNghbrPeer = true;
	}

	private void processBitFieldMessage(Message message) {
		try {
			requestProcessor.addMessage(message);
			if (isHandshakeRcvd && isHandShakeSent) {
				startMeasuringDownloadTime();
			}

		} catch (Exception e) {
			logger.error(CLASS_NAME, "processBitFieldMessage", e);
		}
	}

	private void processHandShakeMessage(HandshakeMessage message) {
		peerId = message.getPeerId();
		sendBitFieldMessage();
		if (!isHandShakeSent) {
			logger.info(CLASS_NAME, "processHandshakeMessage", "Handshake message is processed. " + "Peer "
					+ manager.getPeerId() + " is connected from Peer " + peerId);
			sendHandShakeMessage();
		}

		isHandshakeRcvd = true;
		if (isHandShakeSent) {
			startMeasuringDownloadTime();
		}
	}

	private void processRequestMessage(Message message) {
		if (!isPeerChoked()) {
			Message bitMessage = manager.genBitMessage(message.getIndex());
			if (bitMessage != null) {
				try {
					peerMesssanger.sendMessage(bitMessage);
				} catch (Exception e) {
					logger.error(CLASS_NAME, "processRequestMessage", e);
				}
			}
		}
	}

	private void processHaveMessage(Message message) {
		logger.info(CLASS_NAME, "processHaveMessage",
				"Recieved HAVE message from " + peerId + " for the piece" + message.getIndex());
		try {
			requestProcessor.addMessage(message);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "processHaveMessage", e);
		}
	}

	private void processInterestedMessage(Message message) {
		logger.info(CLASS_NAME, "processInterestedMessage", "Recieved interested message from " + peerId);
	}

	private void processNotInterestedMessage(Message message) {
		logger.info(CLASS_NAME, "processNotInterestedMessage", "Recieved not interested message from " + peerId);
	}

	synchronized boolean sendHandShakeMessage() {
		try {
			HandshakeMessage message = new HandshakeMessage();
			message.setPeerId(manager.getPeerId());
			peerMesssanger.sendMessage(message);
			isHandShakeSent = true;
			logger.info(CLASS_NAME, "sendHandshakeMessage",
					"Handshake Message (P2PFILESHARINGPROJ" + manager.getPeerId() + ") sent");
			return true;
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendHandShakeMessage", e);
		}

		return false;
	}

	synchronized void sendBitFieldMessage() {
		try {
			Message message = manager.getBitFieldMessage();
			logger.info(CLASS_NAME, "sendBitFieldMessage", "Sending field for index" + message.getIndex());
			peerMesssanger.sendMessage(message);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendBitFieldMessage", e);
		}

	}

	public void sendInterestedMessage(Message message) {
		try {
			if (!isChokedNghbrPeer) {
				peerMesssanger.sendMessage(message);
			}
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendInterestedMessage", e);
		}
	}

	public void sendNotInterestedMessage(Message message) {
		try {
			peerMesssanger.sendMessage(message);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendNotInterestedMessage", e);
		}
	}

	public void sendRequestMessage(Message message) {
		try {
			if (!isChokedNghbrPeer) {
				peerMesssanger.sendMessage(message);
			}
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendRequestMessage", e);
		}
	}

	public void sendChokeMessage(Message message) {
		try {
			if (!isPeerChoked()) {
				startMeasuringDownloadTime();
				setChk(true);
				peerMesssanger.sendMessage(message);
			}
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendChokeMessage", e);
		}
	}

	public void sendUnchokeMessage(Message message) {
		try {
			if (isPeerChoked()) {
				startMeasuringDownloadTime();
				setChk(false);
				peerMesssanger.sendMessage(message);
			}
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendUnchokeMessage", e);
		}
	}

	public void processUnchokeMessage(Message message) {
		try {
			peerMesssanger.sendMessage(message);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "processUnchokeMessage", e);
		}
	}

	public void sendHaveMessage(Message message) {
		try {
			peerMesssanger.sendMessage(message);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendHaveMessage", e);
		}
	}

	public void sendShutdownMessage(Message message) {
		try {
			peerMesssanger.sendMessage(message);
		} catch (Exception e) {
			logger.error(CLASS_NAME, "sendShutdownMessage", e);
		}
	}

	private void startMeasuringDownloadTime() {
		downloadStartTime = System.currentTimeMillis();
		dataSize = 0;
	}

	public double downloadSpeed() {
		long timePeriod = System.currentTimeMillis() - downloadStartTime;
		if (timePeriod != 0) {
			return ((dataSize * 1.0) / (timePeriod * 1.0));
		} else {
			return 0;
		}
	}

	public void handleShutDownMessage(Message message) {
		manager.setFileDownloadComplete(peerId);
	}

	private void setChk(boolean message) {
		isChoked = message;
	}

	public boolean isPeerChoked() {
		return isChoked;
	}

	public String getPeerId() {
		return peerId;
	}

	synchronized public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public boolean isPreviousMessageReceived() {
		return lastMessageReceived;
	}

	public void setPreviousMessageRcvd(boolean lastMessageReceived) {
		this.lastMessageReceived = lastMessageReceived;
	}

	public boolean isHandshakeRcvd() {
		return isHandshakeRcvd;
	}

}
