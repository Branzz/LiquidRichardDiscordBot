package com.wordpress.brancodes.messaging.reactions.message.commands;

import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;

import javax.annotation.RegEx;

public class CommandOption extends Command {

	protected CommandOption() {
	}

	public static abstract class Builder<T extends CommandOption, B extends Builder<T, B>> extends Command.Builder<T, B> {

		public Builder(final String name, final String regex, final UserCategoryType userCategoryType, final ReactionChannelType channelCategory) {
			super(name, regex, userCategoryType, channelCategory);
		}

	}

	public static final class CommandOptionBuilder extends CommandOption.Builder<CommandOption, CommandOptionBuilder> {
		public CommandOptionBuilder(String name, @RegEx String regex, UserCategoryType userCategoryType, ReactionChannelType channelCategory) {
			super(name, regex, userCategoryType, channelCategory);
		}
		@Override public CommandOption build() { return object; }
		@Override protected CommandOption createObject() { return new CommandOption(); }
		@Override protected CommandOptionBuilder thisObject() { return this; }
	}

}
