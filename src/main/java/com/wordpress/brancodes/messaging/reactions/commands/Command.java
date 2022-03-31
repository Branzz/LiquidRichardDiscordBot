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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends Reaction {

	protected String description;
	protected boolean deniable;

	protected Command(String name, Matcher matcher, boolean deactivated,
					  UserCategory userCategory, ReactionChannelType channelCategory,
					  String description, boolean deniable,
					  Function<Message, ReactionResponse> executeResponse, BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse) {
		super(name, matcher, deactivated, userCategory, channelCategory, executeResponse, executeMatcherResponse);
		this.description = description;
		this.deniable = deniable;
	}

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

	public static class Builder extends Reaction.Builder {
		protected String description = null;
		protected boolean deniable = false;

		public Builder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}

		public Builder helpPanel(String description) {
			this.description = description;
			return this;
		}

		public Builder deniable() {
			this.deniable = true;
			return this;
		}

		public Builder deactivated() {
			super.deactivated();
			return this;
		}

		public Builder execute(Consumer<Message> executeResponse) {
			super.execute(executeResponse);
			return this;
		}

		public Builder execute(BiConsumer<Message, Matcher> executeMatcherResponse) {
			super.execute(executeMatcherResponse);
			return this;
		}

		public Builder executeStatus(Function<Message, Boolean> executeResponse) {
			super.executeStatus(executeResponse);
			return this;
		}

		public Builder executeStatus(BiFunction<Message, Matcher, Boolean> executeMatcherResponse) {
			super.executeStatus(executeMatcherResponse);
			return this;
		}

		public Builder executeResponse(Function<Message, ReactionResponse> executeResponse) {
			super.executeResponse(executeResponse);
			return this;
		}

		public Builder executeResponse(BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse) {
			super.executeResponse(executeMatcherResponse);
			return this;
		}

		public Command build() {
			if (executeResponse == null && executeMatcherResponse == null)
				throw new IllegalArgumentException("Must define execute");
			final Command command = new Command(name, getMatcher(regex), deactivated, userCategory, channelCategory,
												description, deniable, executeResponse, executeMatcherResponse);
//			Command command = new Command(description, deniable, deactivated);
			command.description = description;
			command.deniable = deniable;
			return command;
		}

	}

}
