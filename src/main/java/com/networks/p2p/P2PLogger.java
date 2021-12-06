package com.networks.p2p;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class P2PLogger extends Logger {
	private static P2PLogger logger;
	private final String logFileName;
	private final String peerId;

	private FileHandler fileHandler;

	private SimpleDateFormat formatter = null;

	public static P2PLogger getInstance(String peerId) {
		if (logger == null) {
			String directory = Constants.LOGDIRECTORY;
			File file = new File(directory);
			if (!file.exists()) {
				file.mkdirs();
			}
			logger = new P2PLogger(peerId, directory + "/" + Constants.LOGPREFIX + peerId + ".log",
					Constants.LOGNAME);
			logger.init();

		}
		return logger;
	}

	public P2PLogger(String peerID, String logFileName, String name) {
		super(name, null);
		this.logFileName = logFileName;
		this.setLevel(Level.ALL);
		this.peerId = peerID;
	}

	private void init() {
		try {
			fileHandler = new FileHandler(logFileName);
			fileHandler.setFormatter(new SimpleFormatter() {

				public synchronized String format(LogRecord record) {
					if (record != null) {
						return record.getMessage();
					}
					return null;
				}

				@Override
				public synchronized String formatMessage(LogRecord record) {
					return this.format(record);
				}
			});

			formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");// ("E, dd MMM yyyy hh:mm:ss a");
			this.addHandler(fileHandler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (fileHandler != null) {
				fileHandler.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void error(String prefix, String message, Exception e) {
		this.log(Level.SEVERE, "[" + prefix + "]: " + message);
		if (e != null) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			this.log(Level.FINEST, "[" + prefix + "]: " + e.getMessage());
			for (StackTraceElement stackTraceElement : stackTrace) {
				this.log(Level.FINEST, stackTraceElement.toString());
			}
		}
	}

	public void error(String message) {
		this.log(message, Level.SEVERE);
	}

	public synchronized void info(String message) {
		this.log(message, Level.INFO);
	}

	public synchronized void log(String message, Level level) {
		String date = formatter.format(Calendar.getInstance().getTime());
		this.log(Level.INFO, "[" + date + "]: Peer [peer_ID " + peerId + "] " + message);
	}

	@Override
	public synchronized void logp(Level level, String className, String methodName, String message) {
		super.logp(level, className, methodName, message + "\n");
	}

	public synchronized void info(String className, String methodName, String message) {
		String date = formatter.format(Calendar.getInstance().getTime());
		this.logp(Level.INFO, className, methodName, "[" + date + "]: Peer [peer_ID " + peerId + "] " + message);
	}
}
