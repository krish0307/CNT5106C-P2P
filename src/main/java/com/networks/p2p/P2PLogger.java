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

			FileAppender appender = new FileAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n"),
					"P2P-" + timeStamp + ".log", true);
//			appender.setFile(timeStamp);
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

	public void info(String className, String method, String msg) {
		logger.info("ClassName-" + className + "." + method + " Message - " + msg);
	}

	public void error(String className, String method, String msg, Exception e) {
		logger.error("ClassName-" + className + "." + method + " Message - " + msg, e);
	}

	public void error(String className, String method, String msg) {
		logger.error("ClassName-" + className + "." + method + " Message - " + msg);
	}

	public void error(String className, String method, Exception e) {
		logger.error("ClassName-" + className + "." + method, e);
	}

	public void warning(String className, String method, String msg) {
		logger.warn("ClassName-" + className + "." + method + " Message - " + msg);
	}

	public void debug(String className, String method, String msg) {
		logger.debug("ClassName-" + className + "." + method + " Message - " + msg);
	}

	public void debug(String className, String method, String msg, Exception e) {
		logger.debug("ClassName-" + className + "." + method + " Message - " + msg, e);
	}

	public static void main(String[] args) {
		P2PLogger log = P2PLogger.getInstance();
		log.info("hello", "hello", "hello");
	}

}
