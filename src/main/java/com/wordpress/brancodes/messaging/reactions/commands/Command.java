package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.MessageReaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.RegEx;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends MessageReaction {

	protected String description;
	protected boolean deniable;
	protected boolean chainable;

	protected Command() { }

	public ReactionResponse execute(Message message, String match) {
		if (matches(match)) {
			if (deniable && deny(message))
				return ReactionResponse.FAILURE;
			ReactionResponse reactionResponse = accept(message);
			if (reactionResponse.status())
				addCooldowns(message);
			return reactionResponse;
		}
		return ReactionResponse.FAILURE;
	}

	private static final @RegEx String aliasesRegexPart = (String) Config.get("aliasesRegex");
	private static final @RegEx String questionRegexPart = "(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)*";

	public static @RegEx String getCommandRegex(String regexQuestion) {
		return getCommandRegex(regexQuestion, questionRegexPart);
	}

	public static @RegEx String getCommandRegex(@RegEx String regexQuestion, @RegEx String questionRegexPart) {
		return "\\s*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s*(,|\\.+|!+|\\s)?\\s*)?(("
			   + aliasesRegexPart + "\\s*(\\?+|\\.+|,|!+)?\\s+" + questionRegexPart + regexQuestion
			   + ")|(" + questionRegexPart + regexQuestion + "\\s*,?\\s+" + aliasesRegexPart //"\\s*(,|\\.+|!+|\\s)?\\s*"
			   + "))\\s*(\\?+|\\.+|,|!+)?\\s*(\\s+(Thanks|Thank\\s+You)\\s*(\\.+|!+)?)?\\s*";
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

	public MessageEmbed toFullString() {
		final EmbedBuilder embedBuilder = getMessageEmbed();
		if (description != null)
			embedBuilder.appendDescription(description);
		if (deniable)
			embedBuilder.setFooter("Is Deniable");
		return embedBuilder.build();
	}

	public static abstract class Builder<T extends Command, B extends Command.Builder<T, B>> extends MessageReaction.Builder<T, B> {

		public Builder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
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
		public CommandBuilder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}
		@Override public Command build() { super.build(); return object; }
		@Override protected Command createObject() { return new Command(); }
		@Override protected CommandBuilder thisObject() { return this; }
	}

}
