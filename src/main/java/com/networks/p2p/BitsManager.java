package com.networks.p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class BitsManager {

	int numOfPieces;
	int size;

	private RandomAccessFile outputStream;
	private FileInputStream inStream;

	private static BitField bitField;
	private static volatile BitsManager instance;

	public synchronized static BitsManager getInstance(boolean fileExists, String peerId) {
		if (instance == null) {
			instance = new BitsManager(fileExists, peerId);

		}
		return instance;
	}

	private BitsManager(boolean isFileExists, String peerID) {

		try {
			CommonData commonFileData = FileParser.getInstance().getCommonFileData();
			size = (commonFileData.getPieceSize());

			numOfPieces = (int) Math.ceil(commonFileData.getFileSize() / (size * 1.0));

			bitField = new BitField(numOfPieces);
			if (isFileExists) {
				bitField.setBitFieldOnForAllIndexes();
			}
			String fileName = commonFileData.getFileName();

			String dirName = peerID;
			File dir = new File(dirName);

			if (!isFileExists) {
				dir.mkdir();
			}

			fileName = dir.getAbsolutePath() + "/" + fileName;
			outputStream = new RandomAccessFile(fileName, "rw");
			outputStream.setLength(commonFileData.getFileSize());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	synchronized public Data get(int i) {
		Data data = new Data(size);
		if (bitField.getBitField(i)) {
			byte[] rBytes = new byte[size];
			int newSize = 0;
			try {
				outputStream.seek(i * size);
				newSize = outputStream.read(rBytes);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			if (newSize != size) {
				byte[] newBytes = new byte[newSize];
				if (newSize >= 0) {
					System.arraycopy(rBytes, 0, newBytes, 0, newSize);
				}
				data.setByteData(newBytes);
			} else {
				data.setByteData(rBytes);
			}
			return data;
		}
		return null;

	}

	synchronized public void writeData(int i, Data data) {
		if (!bitField.getBitField(i)) {
			try {
				outputStream.seek(i * size);
				outputStream.write(data.getByteData());
				bitField.setBitField(i, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	synchronized public int[] getMissingData() {

		int missingCount = bitField.getMissingInfo();
		int[] missingData = new int[missingCount];
		int i = 0;
		for (int val : bitField.getMissingIndices()) {
			missingData[i++] = val;
		}
		return missingData;

	}

	public synchronized boolean isDownloadComplete() {
		return bitField.isDownloadComplete();
	}

	public BitField getBitField() {
		return bitField;
	}

	synchronized public void close() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			if (inStream != null) {
				inStream.close();
			}
		} catch (Exception ignore) {
		}

	}
}
