package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.RegEx;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends Reaction {

	protected String description;
	protected boolean deniable;
	protected boolean chainable;

	protected Command() { }

	// protected Command(String name, Matcher matcher, boolean deactivated,
	// 				  UserCategory userCategory, ReactionChannelType channelCategory,
	// 				  Function<Message, ReactionResponse> executeResponse, BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse,
	// 				  String description, boolean deniable, boolean chainable) {
	// 	super(name, matcher, deactivated, userCategory, channelCategory, executeResponse, executeMatcherResponse);
	// 	this.description = description;
	// 	this.deniable = deniable;
	// 	this.chainable = chainable;
	// }

	@Override
	public ReactionResponse execute(Message message) {
		if (deactivated)
			return new ReactionResponse(false);
		return execute(message, message.getContentRaw());
	}

	public ReactionResponse execute(Message message, String match) {
		if (matcher.reset(match).results().findAny().isPresent()) {
		// if (matcher.reset(match).matches()) {
			if (deniable && deny(message))
				return new ReactionResponse(false);
			else {
				return accept(message);
			}
		}
		return new ReactionResponse(false);
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

	public MessageEmbed toFullString() {
		final EmbedBuilder embedBuilder = getMessageEmbed();
		if (description != null)
			embedBuilder.appendDescription(description);
		if (deniable)
			embedBuilder.setFooter("Is Deniable");
		return embedBuilder.build();
	}

	public static abstract class Builder<T extends Command, B extends Command.Builder<T, B>> extends Reaction.Builder<T, B> {

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

		public CommandBuilder(final String name, final String regex, final UserCategory userCategory, final ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}

		public Command build() {
			// final Command command = new Command(name, matcher, deactivated, userCategory, channelCategory, executeResponse, executeMatcherResponse, description, deniable, chainable);
//			Command command = new Command(description, deniable, deactivated);

			// command.description = description;
			// command.deniable = deniable;
			return object;
		}

		@Override
		protected Command createObject() {
			return new Command();
		}

		@Override
		protected CommandBuilder thisObject() {
			return this;
		}

	}

}
