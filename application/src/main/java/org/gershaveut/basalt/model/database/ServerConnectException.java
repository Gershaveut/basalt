package org.gershaveut.basalt.model.database;

public class ServerConnectException extends Exception {
	public ServerConnectException(String message) {
		super(message);
	}
	
	public ServerConnectException() {
		this("Server connect error");
	}
}
