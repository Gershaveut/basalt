package dev.code_offline.basalt.view.start;

public class UnknownException extends RuntimeException {
	public UnknownException(String message) {
		super(message);
	}
	
	public UnknownException(Exception exception) {
		this(exception.getMessage());
	}
}
