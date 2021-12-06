package com.networks.p2p;

import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageQueue implements Runnable {
	private static final String LOGGER_PREFIX = MessageQueue.class.getSimpleName();

	private ObjectOutputStream outputStream = null;
	private BlockingQueue<IMessage> messageQueue;

	public MessageQueue(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
		messageQueue = new ArrayBlockingQueue<>(100);

	}

	public void run() {
		while (true) {
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

	public void sendMessage(IMessage message) throws InterruptedException {
		if (messageQueue != null) {
			messageQueue.put(message);
		}
	}

	public void kill() {
		if (messageQueue != null && messageQueue.size() != 0) {
			messageQueue.clear();
		}
		messageQueue = null;
	}

}