package com.networks.p2p;

public class HandshakeMessage implements IMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String peerId;

	public HandshakeMessage() {
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


}
