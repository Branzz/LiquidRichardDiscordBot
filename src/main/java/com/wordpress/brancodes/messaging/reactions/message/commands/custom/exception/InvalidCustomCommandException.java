package com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception;

public class InvalidCustomCommandException extends RuntimeException {

	public InvalidCustomCommandException() {
	}

	public InvalidCustomCommandException(final String message) {
		super(message);
	}

}
