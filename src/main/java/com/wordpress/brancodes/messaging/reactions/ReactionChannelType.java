package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.ChannelType;
public enum ReactionChannelType {

	GUILD("Guild"), PRIVATE("DM"), GUILD_AND_PRIVATE("Guild And DM");

	private final String displayName;

	ReactionChannelType(final String displayName) {
		this.displayName = displayName;
	}

	public boolean inRange(ChannelType JDAChannelType) {
		switch (this) {
			case GUILD:
				return JDAChannelType == ChannelType.TEXT;
			case PRIVATE:
				return JDAChannelType == ChannelType.PRIVATE;
			case GUILD_AND_PRIVATE:
				return JDAChannelType == ChannelType.TEXT || JDAChannelType == ChannelType.PRIVATE;
			default:
				return false;
		}
	}

	@Override
	public String toString() {
		return displayName;
	}

}
