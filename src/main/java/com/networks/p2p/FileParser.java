package com.networks.p2p;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FileParser {
	private final static String COMMENT_CHAR = "#";
	private static final String CLASS_NAME = FileParser.class.getCanonicalName();
	private HashMap<String, PeerData> peerIdToData;
	private static FileParser instance;
	private CommonData data;
	private P2PLogger logger = P2PLogger.getInstance();

	public static FileParser getInstance() {
		if (instance == null) {
			instance = new FileParser();
		}
		return instance;
	}

	public List<PeerData> parsePeersFile(String fileLoc) {
		peerIdToData = new HashMap<>();
		List<PeerData> peersData = new ArrayList<PeerData>();
		int i = 0;
		FileReader inputFileReader = null;
		BufferedReader in = null;
		try {
			inputFileReader = new FileReader(fileLoc);
			in = new BufferedReader(inputFileReader);
			for (String line; (line = in.readLine()) != null;) {
				line = line.trim();
				if ((line.length() <= 0) || (line.startsWith(COMMENT_CHAR))) {
					continue;
				}
				String[] tokens = line.split("\\s+");
				if (tokens.length != 4) {
					throw new ParseException(line, i);
				}
				final boolean peerHasFile = (tokens[3].trim().compareTo("1") == 0);
				PeerData data = new PeerData(tokens[0].trim(), tokens[1].trim(), tokens[2].trim(), peerHasFile);
				peersData.add(data);
				peerIdToData.put(data.getPeerId(), data);
				i++;
			}
		} catch (Exception e) {
			logger.error(CLASS_NAME, "parsePeersFile", e);
		} finally {
			try {
				inputFileReader.close();
				in.close();
			} catch (Exception e) {
				logger.error(CLASS_NAME, "parsePeersFile", e);
			}
		}
		logger.info(CLASS_NAME, "parsePeersFile", "Parsing is finished with data" + peersData.toString());
		return peersData;
	}

	public CommonData parseCommonFile(String fileLoc) {
		data = new CommonData();
		try {
			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
			for (String line; (line = in.readLine()) != null;) {
				String[] config = line.split(" ");
				String name = config[0];
				String value = config[1];

				if (name.equals("NumberOfPreferredNeighbors")) {
					data.setNumberOfPreferredNeighbors(Integer.parseInt(value) + 1);
				} else if (name.equals("UnchokingInterval")) {
					data.setUnchokingInterval(Integer.parseInt(value));
				} else if (name.equals("OptimisticUnchokingInterval")) {
					data.setOptimisticUnchokingInterval(Integer.parseInt(value));
				} else if (name.equals("FileName")) {
					data.setFileName(value);
				} else if (name.equals("FileSize")) {
					data.setFileSize(Integer.parseInt(value));
				} else if (name.equals("PieceSize")) {
					data.setPieceSize(Integer.parseInt(value));
				}
			}
			in.close();

		} catch (Exception e) {
			logger.error(CLASS_NAME, "parseCommonFile", e);
		}
		logger.info(CLASS_NAME, "parseCommonFile", "Parsing is finished with data" + data.toString());
		return data;
	}

	public CommonData getCommonFileData() {
		if (data == null) {
			parseCommonFile(Constants.CONFIGURATION_FILE);
		}
		return data;
	}

	public HashMap<String, PeerData> getIdToPeerDataMap() {
		if (peerIdToData == null) {
			parsePeersFile(Constants.PEER_INFO_FILE);
		}
		return peerIdToData;
	}

	public List<PeerData> getNeighbhourPeersData(String peerId) {
		return peerIdToData.values().stream().filter(data -> !data.getPeerId().equals(peerId))
				.collect(Collectors.toList());
	}
}
