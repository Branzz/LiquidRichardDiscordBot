package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.RegEx;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends Reaction {

	protected String description = null;
	protected boolean deniable = false;
	protected boolean deactivated = false;

	protected Command(String name, @RegEx String regex, UserCategory category, ReactionChannelType channelCategory) {
		super(name, regex, category, channelCategory);
	}

	@Override
	public boolean execute(Message message) {
		return execute(message, message.getContentRaw());
	}

	public boolean execute(Message message, String match) {
		if (matcher.reset(match).results().findAny().isPresent()) {
		// if (matcher.reset(match).matches()) {
			if (deniable && deny(message))
				return false;
			else {
				return accept(message);
			}
		}
		return false;
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

	public boolean isDeactivated() {
		return deactivated;
	}

	public MessageEmbed toFullString() {
		final EmbedBuilder embedBuilder =
				new EmbedBuilder().setTitle(name)
								  .setColor(Color.ORANGE)
								  .addField("RegEx", matcher.pattern().toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\*", "\\\\*"), true)
								  .addField("User", category.toString(), true)
								  .addField("Location", channelCategory.toString(), true);
		if (description != null)
			embedBuilder.appendDescription(description);
		if (deniable)
			embedBuilder.setFooter("Is Deniable");
		return embedBuilder.build();
	}

	public static class Builder {
		private String regex;
		private String name;
		private UserCategory category;
		private ReactionChannelType channelCategory;
		private Function<Message, Boolean> executeResponse;
		private BiFunction<Message, Matcher, Boolean> executeMatcherResponse;
		private String description;
		private boolean deniable;
		private boolean deactivated;

		public Builder(String name, @RegEx String regex, UserCategory category, ReactionChannelType channelCategory) {
			this.regex = regex;
			this.name = name;
			this.category = category;
			this.channelCategory = channelCategory;
			// this.executeResponse = executeResponse;
			// executeMatcherResponse = null;
			description = null;
			deniable = false;
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
			this.deactivated = true;
			return this;
		}

		public Builder executeStatus(Function<Message, Boolean> executeResponse) {
			this.executeResponse = executeResponse;
			return this;
		}

		public Builder executeStatus(BiFunction<Message, Matcher, Boolean> executeMatcherResponse) {
			this.executeMatcherResponse = executeMatcherResponse;
			return this;
		}

		public Builder execute(Consumer<Message> executeResponse) {
			this.executeResponse = m -> {
				executeResponse.accept(m);
				return true;
			};
			return this;
		}

		public Builder execute(BiConsumer<Message, Matcher> executeMatcherResponse) {
			this.executeMatcherResponse = (m, r) -> {
				executeMatcherResponse.accept(m, r);
				return true;
			};
			return this;
		}

		public Command build() {
			if (executeResponse == null && executeMatcherResponse == null)
				throw new IllegalArgumentException("Must define execute");
			final Command command = new Command(name, regex, category, channelCategory);
			if (executeResponse == null)
				command.executeMatcherResponse = executeMatcherResponse;
			else
				command.executeResponse = executeResponse;
			command.description = description;
			command.deniable = deniable;
			command.deactivated = deactivated;

			return command;
		}

	}

}
