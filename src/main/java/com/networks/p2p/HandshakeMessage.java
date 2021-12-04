package com.networks.p2p;

/**
 * HandshakeMessage
 */
public class HandshakeMessage implements IMessage {
	private static int COUNT = 0;

	private final int messageNumber;
	private String peerId;

	public HandshakeMessage() {
		messageNumber = ++COUNT;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public String getPeerId() {
		return peerId;
	}

	public MessageType getType() {
		return MessageType.HANDSHAKE;
	}

	public int getLength() {
		return 0;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

}
