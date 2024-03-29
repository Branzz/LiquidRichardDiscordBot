package com.wordpress.brancodes.messaging.reactions.message.commands;

import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.message.MessageReaction;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.RegEx;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends MessageReaction {

	protected String description;
	protected boolean deniable;
	protected boolean chainable;

	protected Command() { }

	@Override
	@OverridingMethodsMustInvokeSuper
	protected boolean canExecute(final Message message) {
		return super.canExecute(message) && !(deniable && deny(message));
	}

	public boolean visibleDescription() {
		return description != null;
	}

	public String getDescription() {
		return description;
	}

	public boolean chainable() {
		return chainable;
	}

	@Override
	protected EmbedBuilder getMessageEmbed() {
		EmbedBuilder embedBuilder = super.getMessageEmbed();
		if (description != null)
			embedBuilder.appendDescription(description);
		if (deniable)
			embedBuilder.setFooter("Is Deniable");
		return embedBuilder;

	}

	public static abstract class Builder<T extends Command, B extends Command.Builder<T, B>> extends MessageReaction.Builder<T, B> {

		public Builder(String name, @RegEx String regex, UserCategoryType userCategoryType, ReactionChannelType channelCategory) {
			super(name, regex, userCategoryType, channelCategory);
		}

		public B helpPanel(String description) {
			object.description = description;
			return thisObject;
		}

		public B deniable() {
			object.deniable = true;
			return thisObject;
		}

		public B andChainable() {
			object.chainable = true;
			return thisObject;
		}

	}

	public static final class CommandBuilder extends Command.Builder<Command, CommandBuilder> {
		public CommandBuilder(String name, @RegEx String regex, UserCategoryType userCategoryType, ReactionChannelType channelCategory) {
			super(name, regex, userCategoryType, channelCategory);
		}
		@Override public Command build() { super.build(); return object; }
		@Override protected Command createObject() { return new Command(); }
		@Override protected CommandBuilder thisObject() { return this; }
	}

}
