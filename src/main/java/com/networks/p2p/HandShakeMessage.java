package com.networks.p2p;

public class HandShakeMessage implements IMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String Header = "P2PFILESHARINGPROJ";
	private String peerId;

	public HandShakeMessage(String peerId) {
		this.peerId = peerId;
	}

	public String getPeerId() {
		return peerId;
	}

	public static String getHeader() {
		return Header;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(getHeader()).append(String.format("%10s", Integer.toBinaryString(0)).replace(' ', '0'))
				.append(this.peerId).toString();
	}

	@Override
	public MessageType getType() {
		return MessageType.HANDSHAKE;
	}
}
