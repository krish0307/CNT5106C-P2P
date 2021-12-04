package com.networks.p2p;

public enum MessageType {
	HANDSHAKE(0), CHOKE(1), UNCHOKE(2), INTERESTED(3), NOTINTERESTED(4), HAVE(5), BITFIELD(6), REQUEST(7), PIECE(8),
	SHUTDOWN(9);

	private final byte value;

	private MessageType(int val) {
		this.value = (byte) val;
	}

	public byte getValue() {
		return value;
	}

}
