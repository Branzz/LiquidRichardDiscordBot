package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.ChannelType;

public enum ReactionChannelType {

	GUILD("Guild"), PRIVATE("DM"), GUILD_AND_PRIVATE("Guild And DM")
	// , CENSOR_GUILD("Specified Server")
	;

	private final String displayName;

	ReactionChannelType(final String displayName) {
		this.displayName = displayName;
	}

	public static ReactionChannelType of(final ChannelType channelType) {
		return ((channelType.isThread() || channelType.isMessage()) && channelType.isGuild()) ? GUILD : channelType == ChannelType.PRIVATE ? PRIVATE : null;
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

	public boolean inRange(ReactionChannelType reactionChannelType) {
		return this == GUILD_AND_PRIVATE || reactionChannelType == GUILD_AND_PRIVATE
			   || (this == GUILD && reactionChannelType == GUILD)
			   || (this == PRIVATE && reactionChannelType == PRIVATE);
	}

	@Override
	public String toString() {
		return displayName;
	}

}
