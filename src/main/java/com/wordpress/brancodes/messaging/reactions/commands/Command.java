package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.ExecuteMatcherResponse;
import com.wordpress.brancodes.messaging.reactions.ExecuteResponse;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.RegEx;

import java.awt.*;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends Reaction {

	protected String description = null;
	protected boolean deniable = false;
	protected boolean deactivated = false;

	public Command(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		super(regex, name, category, channelCategory, executeResponse);
	}

	public Command(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteMatcherResponse executeMatcherResponse) {
		super(regex, name, category, channelCategory, executeMatcherResponse);
	}

	@Override
	public boolean execute(Message message) {
		return execute(message, message.getContentRaw());
	}

	public boolean execute(Message message, String match) {
		if (matcher.reset(match).matches()) {
			if (deniable && deny(message))
				return false;
			else {
				accept(message);
				return true;
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
		private ExecuteResponse executeResponse;
		private ExecuteMatcherResponse executeMatcherResponse;
		private String description;
		private boolean deniable;
		private boolean deactivated;

		public Builder(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
			this.regex = regex;
			this.name = name;
			this.category = category;
			this.channelCategory = channelCategory;
			this.executeResponse = executeResponse;
			executeMatcherResponse = null;
			description = null;
			deniable = false;
		}

		public Builder(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteMatcherResponse executeMatcherResponse) {
			this.regex = regex;
			this.name = name;
			this.category = category;
			this.channelCategory = channelCategory;
			this.executeResponse = null;
			this.executeMatcherResponse = executeMatcherResponse;
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

		public Builder deactivate() {
			this.deactivated = true;
			return this;
		}

		public Command build() {
			final Command command = getNew();
			command.description = description;
			command.deniable = deniable;
			command.deactivated = deactivated;
			return command;
		}

		private Command getNew() {
			if (executeResponse == null)
				return new Command(regex, name, category, channelCategory, executeMatcherResponse);
			else
				return new Command(regex, name, category, channelCategory, executeResponse);
		}

	}

}
