package com.networks.p2p;

import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PeerMessanger implements Runnable {
	/* log */
	private static final String LOGGER_PREFIX = PeerMessanger.class.getSimpleName();

	private ObjectOutputStream outputStream = null;
	private BlockingQueue<IMessage> messageQueue;
	private boolean shutDown = false;

	/**
	 * get new instance of PeerMessageSender
	 * @param outputStream
	 * @return
	 */
	public static PeerMessanger getInstance(ObjectOutputStream outputStream) {
		PeerMessanger peerMessageSender = new PeerMessanger();
		if (!peerMessageSender.init()) {
			peerMessageSender.destroy();
			return null;
		}

		peerMessageSender.outputStream = outputStream;
		return peerMessageSender;
	}

	public void destroy() {
		if (messageQueue != null && messageQueue.size() != 0) {
			messageQueue.clear();
		}
		messageQueue = null;
	}

	private boolean init() {
		messageQueue = new ArrayBlockingQueue<>(100);
		return true;
	}


	public void run() {
		if (messageQueue == null) {
			throw new IllegalStateException(LOGGER_PREFIX + ": This object is not initialized properly. This might be result of calling deinit() method");
		}

		while (true) {
			if (shutDown) break;
			try {
				IMessage message = messageQueue.take();
				outputStream.writeUnshared(message);
				outputStream.flush();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * sendMessage
	 * @param message
	 * @throws InterruptedException
	 */
	public void sendMessage(IMessage message) throws InterruptedException {
		if (messageQueue != null) {
			messageQueue.put(message);
		} 
	}

	public void shutdown() {
		shutDown = true;
	}
}
