package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.ExecuteResponse;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.RegEx;

import static com.wordpress.brancodes.bot.LiquidRichardBot.deny;

public class Command extends Reaction {

	protected String description = null;
	protected boolean deniable = false;

	public Command(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		super(regex, name, category, channelCategory, executeResponse);
	}

	public Command(@RegEx String regex, String name, boolean deniable, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		super(regex, name, category, channelCategory, executeResponse);
		this.deniable = deniable;
	}

	public Command(@RegEx String regex, String name, String description, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		super(regex, name, category, channelCategory, executeResponse);
		this.description = description;
	}

	public Command(@RegEx String regex, String name, String description, boolean deniable, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		super(regex, name, category, channelCategory, executeResponse);
		this.description = description;
		this.deniable = deniable;
	}

	@Override
	public boolean execute(Message message) {
		return execute(message, true);
	}

	protected boolean execute(Message message, boolean contentDisplay) {
		return execute(message, contentDisplay ? message.getContentDisplay() : message.getContentRaw());
	}

	public boolean execute(Message message, String match) {
		if (matcher.reset(match).matches()) {
			if (deniable && deny(message))
				return false;
			else {
				executeResponse.execute(message);
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
		return "\\s*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s*(,|\\.+|!+|\\s)?\\s*)?((" + aliasesRegexPart + "\\s*(\\?+|\\.+|,|!+)?\\s+" + questionRegexPart + regexQuestion
						  + ")|(" + questionRegexPart + regexQuestion + "\\s*,?\\s+" + aliasesRegexPart //"\\s*(,|\\.+|!+|\\s)?\\s*"
						  + "))\\s*(\\?+|\\.+|,|!+)?\\s*(\\s+(Thanks|Thank\\s+You)\\s*(\\.+|!+)?)?\\s*";
	}

	public boolean visibleDescription() {
		return description != null;
	}

	public String getDescription() {
		return description;
	}

}
