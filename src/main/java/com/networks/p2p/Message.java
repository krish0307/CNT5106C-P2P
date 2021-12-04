package com.networks.p2p;

/**
 * Peer2PeerMessage
 */
public class Message implements IMessage{
	private static int COUNT = 0;

	private Piece data;
	private BitField bitFieldHandler = null;
	private int index;
	private int length;

	private MessageType messageType;
	public int messageNumber = 0;

	private Message(){
		messageNumber = ++COUNT;
	}
	
	public static Message create(){
		return new Message();
	}

	public MessageType getType() {
		return this.messageType;
	}

	public int getLength() {
		return this.length;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

	public byte[] getMessage(){
		return null;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Piece getData() {
		return data;
	}

	public BitField getBitFieldHandler() {
		return bitFieldHandler;
	}

	public void setBitFieldHandler(BitField bitFieldHandler) {
		this.bitFieldHandler = bitFieldHandler;
	}

	public void setData(Piece data) {
		this.data = data;
	}

	public void setLength(int length) {
		this.length = length;
	}

//	public int getMessageType() {
//		return messageType;
//	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

}
