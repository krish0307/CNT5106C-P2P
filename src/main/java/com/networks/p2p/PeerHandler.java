package com.networks.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerHandler implements Runnable {
	private Socket socket;
	private String peerId;
	private boolean isHandShakeSent;
	private boolean isHandShakeReceived;
	private P2PLogger logger = P2PLogger.getInstance();

	public PeerHandler(Socket socket, String peerId, PeerManager manager) {
		this.socket = socket;
		this.peerId = peerId;
	}

	@Override
	public void run() {
		ObjectInputStream objectInputStream = null;
		ObjectOutputStream neighborPeerOutputStream;
		try {
			neighborPeerOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectInputStream = new ObjectInputStream(socket.getInputStream());

			sendHandShakeMessage(neighborPeerOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				IMessage message = (IMessage) objectInputStream.readObject();
				switch (message.getType()) {
				case HANDSHAKE:
					if (message instanceof HandShakeMessage) {
						HandShakeMessage handShakeMessage = (HandShakeMessage) message;
						processHandshakeMessage(handShakeMessage);
					}
					break;
				case CHOKE:
					break;
				default:
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void sendHandShakeMessage(ObjectOutputStream neighborPeerOutputStream) throws IOException {
		HandShakeMessage msg = new HandShakeMessage(peerId);
		neighborPeerOutputStream.writeUnshared(msg);
		neighborPeerOutputStream.flush();
		isHandShakeSent = true;
	}

	private void processHandshakeMessage(HandShakeMessage message) {
		String peerIdInMsg = message.getPeerId();
		if (peerIdInMsg == peerId && isHandShakeSent) {
			isHandShakeReceived = true;
			sendBitFieldMessage();

		} else {
			logger.error(this.getClass().getSimpleName(), "processHandshakeMessage",
					"PeerId in message is different for this peerhandler or handshake not sent");
		}
	}

	private void sendBitFieldMessage() {
		
	}

}
