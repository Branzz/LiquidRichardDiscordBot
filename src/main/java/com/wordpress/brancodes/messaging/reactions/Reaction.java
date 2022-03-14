package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.RegEx;
import java.security.InvalidParameterException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reaction { // weird encapsulation between this and command; Commands.java -> Reactions.java ?

	protected Matcher matcher;
	protected String name;
	protected UserCategory category;
	protected ReactionChannelType channelCategory;
	protected Function<Message, Boolean> executeResponse;
	protected BiFunction<Message, Matcher, Boolean> executeMatcherResponse;

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, Function<Message, Boolean> executeResponse) {
		this(name, regex, category, channelCategory);
		this.executeResponse = executeResponse;
		this.executeMatcherResponse = null;
	}

	public Reaction(@RegEx String regex, String name, UserCategory category, ReactionChannelType channelCategory, BiFunction<Message, Matcher, Boolean> executeMatcherResponse) {
		this(name, regex, category, channelCategory);
		this.executeResponse = null;
		this.executeMatcherResponse = executeMatcherResponse;
	}

	public Reaction(String name, @RegEx String regex, UserCategory category, ReactionChannelType channelCategory) {
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

	protected boolean accept(final Message message) {
		if (executeResponse != null)
			return executeResponse.apply(message);
		else
			return executeMatcherResponse.apply(message, matcher);
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
		return getMatcher(regex, "");
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
