package com.networks.p2p;

import java.io.Serializable;


public class Data implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] byteData;
	int size;

	public Data(int size) {
		this.size = size;
	}

	public byte[] getByteData() {
		return byteData;
	}

	public void setByteData(byte[] byteData) {
		this.byteData = byteData;
	}

	public int getSize() {
		if (byteData == null) {
			return -1;
		} else {
			return byteData.length;
		}
	}
}
