package com.networks.p2p;


public class CommonData {
	private int numberOfPreferredNeighbors;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;

	public int getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}

	public int getUnchokingInterval() {
		return unchokingInterval;
	}

	public int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public CommonData setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors) {
		this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
		return this;
	}

	public CommonData setUnchokingInterval(int unchokingInterval) {
		this.unchokingInterval = unchokingInterval;
		return this;
	}

	public CommonData setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
		this.optimisticUnchokingInterval = optimisticUnchokingInterval;
		return this;
	}

	public CommonData setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public CommonData setFileSize(int fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public CommonData setPieceSize(int pieceSize) {
		this.pieceSize = pieceSize;
		return this;
	}

}
