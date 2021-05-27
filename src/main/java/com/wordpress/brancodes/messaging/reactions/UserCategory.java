package com.wordpress.brancodes.messaging.reactions;

public enum UserCategory {

	OWNER("Bot Owner"),
	MOD("Moderator"),
	DEFAULT("Public"),
	BOT("Other Bots"),
	SELF("Liquid Richard");

	private final String displayName;

	UserCategory(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean inRange(final UserCategory userCategory) {
		return this.compareTo(userCategory) >= 0;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
