package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.Message;

import javax.annotation.RegEx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Reaction {

	protected Matcher matcher;
	protected Matcher morseMatcher;
	protected String name;
	protected UserCategory category;
	protected ReactionChannelType channelCategory;
	protected ExecuteResponse executeResponse;

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory) {
		this.matcher = getMatcher(regex);
		this.name = name;
		this.category = category;
		this.channelCategory = channelCategory;
		executeResponse = message -> {
		};
	}

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		this.matcher = getMatcher(regex);
		this.name = name;
		this.category = category;
		this.channelCategory = channelCategory;
		this.executeResponse = executeResponse;
	}

	public boolean execute(final Message message) {
		if (matcher.reset(message.getContentRaw()).matches()) {
			executeResponse.execute(message);
			return true;
		}
		return false;
	}

	// protected boolean matchesCommand(final String message) {
	// 	return matcher.reset(message).matches();
	// }

	// public boolean executeIfMatches(final Message message) {
	// 	if (matchesCommand(message.getContentRaw())) {
	// 		execute(message);
	// 		return true;
	// 	}
	// 	return false;
	// }

	protected static Matcher getMatcher(@RegEx String regex) {
		return Pattern.compile(regex).matcher("");
	}

	public ReactionChannelType getChannelType() {
		return channelCategory;
	}

	public UserCategory getUserCategory() {
		return category;
	}

	public String toString() {
		return name;
	}

}
