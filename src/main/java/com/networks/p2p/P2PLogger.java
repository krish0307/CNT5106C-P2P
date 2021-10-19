package com.networks.p2p;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class P2PLogger {
	static final Logger logger = Logger.getLogger(P2PLogger.class);
	private static P2PLogger instance = null;

	private P2PLogger() {
		try {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

			FileAppender appender = new FileAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n"), "P2P-" + timeStamp,
					true);
			appender.activateOptions();
			Logger.getRootLogger().addAppender(appender);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static P2PLogger getInstance() {
		if (instance == null) {
			instance = new P2PLogger();
		}
		return instance;
	}

	public void socketStarted() {
		logger.info("Server has started");
	}

	public void writeLog(String msg) {
		writeLog(msg, null);
	}

	public void writeLog(String msg, MessageType type) {
		switch (type) {
		case HANDSHAKE:
			break;
		case CHOKE:
			break;
		case UNCHOKE:
			break;
		case INTERESTED:
			break;
		case NOTINTERESTED:
			break;
		case HAVE:
			break;
		case BITFIELD:
			break;
		case REQUEST:
			break;
		case PIECE:
			break;

		default:
			logger.info(msg);
		}

	}

}
