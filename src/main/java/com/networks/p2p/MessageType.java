package com.networks.p2p;

public enum MessageType {
	HANDSHAKE,CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, HAVE, BITFIELD, REQUEST, PIECE;

	private final byte value;

	private MessageType() {
		this.value = (byte) ordinal();
	}

	public byte getValue() {
		return value;
	}

}
