package dev.code_offline.basalt.model.recent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RecentStart {
	private final String address;
	private final boolean offline;

	@JsonCreator
	public RecentStart(@JsonProperty(value = "address", required = true) String address, @JsonProperty(value = "offline", required = true) boolean isOffline) {
		this.address = address;
		this.offline = isOffline;
	}
	
	@Override
	public String toString() {
		return address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public boolean isOffline() {
		return offline;
	}
}
