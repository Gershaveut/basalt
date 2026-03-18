package org.gershaveut.basalt.view.start;

public class UnknownException extends RuntimeException {
	public UnknownException(String message) {
		super(message);
	}
	
	public UnknownException(Exception exception) {
		this(exception.getMessage());
	}
}
