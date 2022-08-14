package com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception;

// appropriate to be RunTime as this is for the programmer to be notified in static initialization
public class CustomCommandCompileErrorException extends RuntimeException {

	public CustomCommandCompileErrorException(String message) {
		super(message);
	}

}
