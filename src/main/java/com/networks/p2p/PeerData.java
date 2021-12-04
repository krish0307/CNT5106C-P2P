package com.networks.p2p;


public class PeerData {

	private String address;
	private int port;
	private String peerId;
	private boolean fileExist;

	public PeerData(String peerId, String peerAddress, String peerPort, boolean hasFile) {
		this.peerId = peerId;
		this.address = peerAddress;
		this.port = Integer.parseInt(peerPort);
		this.fileExist = hasFile;
	}

	public PeerData() {

	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public boolean isFileExist() {
		return fileExist;
	}

	public void setFileExist(boolean fileExist) {
		this.fileExist = fileExist;
	}
}
