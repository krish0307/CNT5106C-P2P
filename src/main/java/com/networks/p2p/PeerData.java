package com.networks.p2p;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerData {

	private final String peerId;
	private final String peerAddress;
	private final String peerPort;
	private final boolean hasFile;
	private AtomicInteger bytesDownloadedFrom;
	private BitSet receivedParts;
	private final AtomicBoolean interested;

	public PeerData(String peerId, String peerAddress, String peerPort, boolean hasFile) {
		this.peerId = peerId;
		this.peerAddress = peerAddress;
		this.peerPort = peerPort;
		this.hasFile = hasFile;
		this.bytesDownloadedFrom = new AtomicInteger(0);
		this.receivedParts = new BitSet();
		this.interested = new AtomicBoolean(false);
	}

	public String getPeerId() {
		return peerId;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public String getPeerPort() {
		return peerPort;
	}

	public boolean hasFile() {
		return hasFile;
	}

	public AtomicInteger getBytesDownloadedFrom() {
		return bytesDownloadedFrom;
	}

	public BitSet getReceivedParts() {
		return receivedParts;
	}

	public AtomicBoolean getInterested() {
		return interested;
	}

}
