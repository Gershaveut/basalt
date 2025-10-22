package dev.code_offline.basalt.model.database;

public class NetworkVersionException extends RuntimeException {
	public NetworkVersionException() {
		super("Network version doesn't match");
	}
}
