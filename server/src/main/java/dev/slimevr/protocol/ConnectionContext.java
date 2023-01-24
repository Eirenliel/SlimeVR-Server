package dev.slimevr.protocol;

import solarxr_protocol.data_feed.DataFeedConfig;

import java.util.ArrayList;
import java.util.List;


public class ConnectionContext {

	private final List<DataFeedConfig> dataFeedConfigList = new ArrayList<>();

	/*
	 * FIXME: I did it in a separate array because it was more convenient than
	 * making a parent object of the DataFeedConfigT idk if it should be a
	 * concern or not, i think it is fine tbh Futurabeast
	 */
	private final List<Long> dataFeedTimers = new ArrayList<>();

	private final List<Integer> subscribedTopics = new ArrayList<>();

	private boolean useSerial = false;
	private boolean useAutoBone = false;

	public List<DataFeedConfig> getDataFeedConfigList() {
		return dataFeedConfigList;
	}

	public List<Long> getDataFeedTimers() {
		return dataFeedTimers;
	}

	public void clearDataFeeds() {
		this.dataFeedConfigList.clear();
		this.dataFeedTimers.clear();
	}

	public List<Integer> getSubscribedTopics() {
		return subscribedTopics;
	}

	public boolean useSerial() {
		return useSerial;
	}

	public void setUseSerial(boolean useSerial) {
		this.useSerial = useSerial;
	}

	public boolean useAutoBone() {
		return useAutoBone;
	}

	public void setUseAutoBone(boolean useAutoBone) {
		this.useAutoBone = useAutoBone;
	}

}
