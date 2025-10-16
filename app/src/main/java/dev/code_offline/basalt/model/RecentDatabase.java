package dev.code_offline.basalt.model;

public class RecentDatabase {
	private final String address;
	private final boolean isOffline;

	public RecentDatabase(String address, boolean isOffline) {
		this.address = address;
		this.isOffline = isOffline;
	}
	
	@Override
	public String toString() {
		return address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public boolean isOffline() {
		return isOffline;
	}
}
