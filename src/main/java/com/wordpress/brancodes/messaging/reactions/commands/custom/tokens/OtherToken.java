package com.wordpress.brancodes.messaging.reactions.commands.custom.tokens;

public class OtherToken implements TokenType {

	private final String value;

	public OtherToken(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
