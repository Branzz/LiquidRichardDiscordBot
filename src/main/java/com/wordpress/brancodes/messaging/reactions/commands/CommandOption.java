package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;

import javax.annotation.RegEx;

public class CommandOption extends Command {

	protected CommandOption() {
	}

	// protected CommandOption(final String name, final Matcher matcher, final boolean deactivated, final UserCategory userCategory, final ReactionChannelType channelCategory, final String description, final boolean deniable, final Function<Message, ReactionResponse> executeResponse, final BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse) {
	// 	super(name, matcher, )
	// }

	public static abstract class Builder<T extends CommandOption, B extends Builder<T, B>> extends Command.Builder<T, B> {

		public Builder(final String name, final String regex, final UserCategory userCategory, final ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}

	}

	public static final class CommandOptionBuilder extends CommandOption.Builder<CommandOption, CommandOptionBuilder> {

		public CommandOptionBuilder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}

		@Override
		protected CommandOption createObject() {
			return new CommandOption();
		}

		@Override
		protected CommandOptionBuilder thisObject() {
			return this;
		}

		public CommandOption build() {
			return object;
		}

	}

}
