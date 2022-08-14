package com.wordpress.brancodes.messaging.reactions.message.commands.custom.types;


public class Void {
	static Void VOID;
	public static Void getInstance() {
		return VOID;
	}
	private Void() {}
}
