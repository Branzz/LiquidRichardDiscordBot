package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.RegEx;
import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reaction {

	protected Matcher matcher;
	protected String name;
	protected UserCategory category;
	protected ReactionChannelType channelCategory;
	protected ExecuteResponse executeResponse;
	protected ExecuteMatcherResponse executeMatcherResponse;

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteResponse executeResponse) {
		this(regex, name, category, channelCategory);
		this.executeResponse = executeResponse;
		this.executeMatcherResponse = null;
	}

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, ExecuteMatcherResponse executeMatcherResponse) {
		this(regex, name, category, channelCategory);
		this.executeResponse = null;
		this.executeMatcherResponse = executeMatcherResponse;
	}

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory) {
		this.matcher = getMatcher(regex);
		if (name.length() > 16)
			throw new InvalidParameterException("name \"" + name + "\" must be 16 characters or less");
		this.name = name;
		this.category = category;
		this.channelCategory = channelCategory;
	}

	public boolean execute(final Message message) {
		if (matcher.reset(message.getContentRaw()).matches()) {
			accept(message);
			return true;
		}
		return false;
	}

	protected void accept(final Message message) {
		if (executeResponse != null)
			executeResponse.accept(message);
		else
			executeMatcherResponse.accept(message, matcher);
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

	public static Matcher getMatcher(@RegEx String regex, String input) {
		return Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS).matcher(input);
	}

	public static Matcher getMatcher(@RegEx String regex) {
		return getMatcher(regex,"");
	}

	public ReactionChannelType getChannelType() {
		return channelCategory;
	}

	public UserCategory getUserCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
