package com.networks.p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

/**
 * Piece Manager
 */
public class BitsManager {

	int numOfPieces; // num of piece
	int size; // piece size

	private RandomAccessFile outStream;
	private FileInputStream inStream;

	private static BitField bitField;
	private static volatile BitsManager instance;

	/**
	 * get instance
	 * 
	 * @param isFileExists
	 * @param peerID
	 * @return
	 */

	private BitsManager(boolean isFileExists, String peerID) {

		// get config info: PieceSize
		CommonData commonFileData = FileParser.getInstance().getCommonFileData();
		size = (commonFileData.getPieceSize());

		// get config info: FileSize
		numOfPieces = (int) Math.ceil(commonFileData.getFileSize() / (size * 1.0));

		try {
			bitField = new BitField(numOfPieces);
			if (isFileExists) {
				bitField.setBitFieldOnForAllIndexes();
			}
			String outputFileName = commonFileData.getFileName();

			String directoryName = peerID;
			File directory = new File(directoryName);

			if (!isFileExists) {
				directory.mkdir();
			}

			outputFileName = directory.getAbsolutePath() + "/" + outputFileName;
			outStream = new RandomAccessFile(outputFileName, "rw");
			outStream.setLength(commonFileData.getFileSize());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized static BitsManager getInstance(boolean isFileExists, String peerID) {
		if (instance == null) {
			instance = new BitsManager(isFileExists, peerID);

		}
		return instance;
	}

	/**
	 * close
	 */
	synchronized public void close() {
		try {
			if (outStream != null) {
				outStream.close();
			}
		} catch (Exception ignore) {
		}

		try {
			if (inStream != null) {
				inStream.close();
			}
		} catch (Exception ignore) {
		}

	}

	/**
	 * Gets the piece of file.
	 * 
	 * @param index
	 * @return
	 */
	synchronized public Piece get(int index) {
		Piece newPiece = new Piece(size);
		if (bitField.getBitField(index)) {
			byte[] readBytes = new byte[size];
			int newSize = 0;
			// have to read this piece from my own output file.
			try {
				outStream.seek(index * size);
				newSize = outStream.read(readBytes);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			if (newSize != size) {
				byte[] newReadBytes = new byte[newSize];
				if (newSize >= 0) {
					System.arraycopy(readBytes, 0, newReadBytes, 0, newSize);
				}
				newPiece.setByteData(newReadBytes);
			} else {
				newPiece.setByteData(readBytes);
			}
			return newPiece;
		} else {
			return null;
		}
	}

	/**
	 * write piece
	 * 
	 * @param index
	 * @param piece
	 */
	synchronized public void write(int index, Piece piece) {
		if (!bitField.getBitField(index)) {
			try {
				// have to write this piece in Piece object array
				outStream.seek(index * size);
				outStream.write(piece.getByteData());
				bitField.setBitField(index, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * the missing piece number.
	 *
	 * @return
	 */
	synchronized public int[] getMissingPieceNumberArray() {

		int missingPieceCount = bitField.getMissingPieceCount();
		int[] missData = new int[missingPieceCount];
		int i = 0;
		for (int val : bitField.getMissingPieceIndices()) {
			missData[i++] = val;
		}
		return missData;

	}

	/**
	 * check file download completed
	 * 
	 * @return
	 */
	public synchronized boolean hasDownloadFileComplete() {
		return bitField.isFileDownloadComplete();
	}

	/**
	 * getBitField
	 * 
	 * @return
	 */
	public BitField getBitField() {
		return bitField;
	}
}
