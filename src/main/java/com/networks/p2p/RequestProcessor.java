package com.networks.p2p;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RequestProcessor implements Runnable {
	private static final String LOGGER_PREFIX = RequestProcessor.class.getSimpleName();

	private BlockingQueue<Message> messageQueue;
	private PeerManager controller;
	private PeerHandler peerHandler;
	private BitField neighborPeerBitFieldhandler = null;

	int[] pieceIndexArray = new int[1000];

	public static RequestProcessor getInstance(PeerManager controller, PeerHandler peerHandler) {

		RequestProcessor requestSender = new RequestProcessor();
		if (!requestSender.init()) {
			requestSender.destroy();
			return null;
		}

		requestSender.controller = controller;
		requestSender.peerHandler = peerHandler;

		return requestSender;
	}

	private boolean init() {
		messageQueue = new ArrayBlockingQueue<>(Constants.QUEUE_SIZE);
		CommonData commonFileData = FileParser.getInstance().getCommonFileData();
		int pieceSize = commonFileData.getPieceSize();
		int numOfPieces = (int) Math.ceil(commonFileData.getFileSize() / (pieceSize * 1.0));
		neighborPeerBitFieldhandler = new BitField(numOfPieces);

		return true;
	}

	public void destroy() {
		if (messageQueue != null && messageQueue.size() != 0) {
			messageQueue.clear();
		}
		messageQueue = null;
	}

	public void run() {
		if (messageQueue == null) {
			throw new IllegalStateException(LOGGER_PREFIX
					+ ": This object is not initialized properly. This might be result of calling deinit() method");
		}

		while (true) {
			try {
				Message message = messageQueue.take();

				Message requestMessage = Message.create();
				requestMessage.setMessageType(MessageType.REQUEST);

				Message interestedMessage = Message.create();
				interestedMessage.setMessageType(MessageType.INTERESTED);

				if (message.getType() == MessageType.BITFIELD) {
					neighborPeerBitFieldhandler = message.getBitField();

					int missingPieceIndex = getPieceNumberToBeRequested();
					if (missingPieceIndex == -1) {
						Message notInterestedMessage = Message.create();
						notInterestedMessage.setMessageType(MessageType.NOTINTERESTED);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					} else {
						interestedMessage.setIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);

						requestMessage.setIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}
				}

				if (message.getType() == MessageType.HAVE) {
					int pieceIndex = message.getIndex();
					try {
						neighborPeerBitFieldhandler.setBitField(pieceIndex, true);
					} catch (Exception e) {
						System.out.println(LOGGER_PREFIX + "[" + peerHandler.getPeerId()
								+ "]: NULL POINTER EXCEPTION for piece Index" + pieceIndex + " ... "
								+ neighborPeerBitFieldhandler);
						e.printStackTrace();
					}

					int missingPieceIndex = getPieceNumberToBeRequested();
					if (missingPieceIndex == -1) {
						Message notInterestedMessage = Message.create();
						notInterestedMessage.setMessageType(MessageType.NOTINTERESTED);
						peerHandler.sendNotInterestedMessage(notInterestedMessage);
					} else {
						if (peerHandler.lastMessageStatus()) {
							peerHandler.setPreviousMessageRcvd(false);
							interestedMessage.setIndex(missingPieceIndex);
							peerHandler.sendInterestedMessage(interestedMessage);

							requestMessage.setIndex(missingPieceIndex);
							peerHandler.sendRequestMessage(requestMessage);
						}
					}
				}

				if (message.getType() == MessageType.PIECE) {
					// supposed to send request message only after piece for previous request
					// message.
					int missingPieceIndex = getPieceNumberToBeRequested();

					if (missingPieceIndex != -1) {
						if (peerHandler.lastMessageStatus()) {
							peerHandler.setPreviousMessageRcvd(false);
							interestedMessage.setIndex(missingPieceIndex);
							peerHandler.sendInterestedMessage(interestedMessage);

							requestMessage.setIndex(missingPieceIndex);
							peerHandler.sendRequestMessage(requestMessage);
						}
					}
				} else if (message.getType() == MessageType.UNCHOKE) {
					// supposed to send request message after receiving unchoke message
					int missingPieceIndex = getPieceNumberToBeRequested();
					peerHandler.setPreviousMessageRcvd(false);
					if (missingPieceIndex != -1) {
						interestedMessage.setIndex(missingPieceIndex);
						peerHandler.sendInterestedMessage(interestedMessage);

						requestMessage.setIndex(missingPieceIndex);
						peerHandler.sendRequestMessage(requestMessage);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	public int getPieceNumberToBeRequested() {
		BitField thisPeerBitFiledHandler = controller.getBitFieldMessage().getBitField();
		int count = 0;
		for (int i = 0; i < neighborPeerBitFieldhandler.getSize() && count < pieceIndexArray.length; i++) {
			if (thisPeerBitFiledHandler.getBitField(i) || !neighborPeerBitFieldhandler.getBitField(i)) {
				continue;
			}
			pieceIndexArray[count] = i;
			count++;
		}

		if (count == 0) {
			return -1;
		}
		Random random = new Random();
		int index = random.nextInt(count);
		return pieceIndexArray[index];
	}

	public void addMessage(Message message) throws InterruptedException {
		if (messageQueue == null) {
			throw new IllegalStateException("");
		} else {
			messageQueue.put(message);
		}
	}

	public boolean isNeighborPeerDownloadedFile() {
		return neighborPeerBitFieldhandler != null && neighborPeerBitFieldhandler.isDownloadComplete();
	}
}