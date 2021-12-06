package com.networks.p2p;

public class Message implements IMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Data data;
	private BitField bitField = null;
	private int index;
	private int length;

	private MessageType messageType;

	private Message(){
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

	public byte[] getMessage(){
		return null;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Data getData() {
		return data;
	}

	public BitField getBitField() {
		return bitField;
	}

	public void setBitField(BitField bitField) {
		this.bitField = bitField;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public void setLength(int length) {
		this.length = length;
	}


	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

}
