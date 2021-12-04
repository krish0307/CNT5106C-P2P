package com.networks.p2p;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BitField implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BitSet bits;
	int size;

	public BitField(int numOfPieces) {
		bits = new BitSet(numOfPieces);
		size = numOfPieces;
	}

	public void setBitFieldOnForAllIndexes() {
		bits.set(0, size);
	}

//	public BitSet getBitField() {
//		return bits;
//	}

	public int getSize() {
		return size;
	}

	public boolean isFileDownloadComplete() {
		return bits.cardinality() == size;
	}

	public int getMissingPieceCount() {
		return size - bits.cardinality();
	}

	public List<Integer> getMissingPieceIndices() {
		List<Integer> missingPieces = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			if (!bits.get(i))
				missingPieces.add(i);
		}
		return missingPieces;
	}

	public void setBitField(int index, boolean status) {
		bits.set(index, status);
	}

	public boolean getBitField(int index) {
		return bits.get(index);
	}


}
