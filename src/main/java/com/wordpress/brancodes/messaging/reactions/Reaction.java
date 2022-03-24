package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.RegEx;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reaction { // weird encapsulation between this and command; Commands.java -> Reactions.java ?

	protected String name;
	protected Matcher matcher;
	protected boolean deactivated;
	protected UserCategory userCategory;
	protected ReactionChannelType channelCategory;
	protected Function<Message, Boolean> executeResponse;
	protected BiFunction<Message, Matcher, Boolean> executeMatcherResponse;

	protected Reaction() { }

	protected Reaction(String name, Matcher matcher, boolean deactivated, UserCategory userCategory,
					   ReactionChannelType channelCategory) {
		this.matcher = matcher;
		if (name.length() > 16)
			throw new InvalidParameterException("name \"" + name + "\" must be 16 characters or less");
		this.name = name;
		this.deactivated = deactivated;
		this.userCategory = userCategory;
		this.channelCategory = channelCategory;
	}

	public boolean execute(final Message message) {
		return execute(message, message.getContentRaw());
	}

	public boolean execute(Message message, String match) {
		if (matcher.reset(match).results().findAny().isPresent()) {
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

	public String getRegex() {
		return matcher.pattern().pattern();
	}

	public ReactionChannelType getChannelType() {
		return channelCategory;
	}

	public UserCategory getUserCategory() {
		return userCategory;
	}

	public String getName() {
		return name;
	}

	public boolean isDeactivated() {
		return deactivated;
	}

	public void deactivate() {
		deactivated = true;
	}

	public void activate() {
		deactivated = false;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * for children classes to add on to it
	 */
	protected EmbedBuilder getMessageEmbed() {
		return new EmbedBuilder().setTitle(name)
						.setColor(Color.ORANGE)
						.addField("RegEx", matcher.pattern().toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\*", "\\\\*"), true)
						.addField("User", userCategory.toString(), true)
						.addField("Location", channelCategory.toString(), true);
	}

	public MessageEmbed toFullString() {
		return getMessageEmbed().build();
	}

	public static class Builder {
		protected String regex;
		protected String name;
		protected boolean deactivated = false;
		protected UserCategory userCategory;
		protected ReactionChannelType channelCategory;
		protected Function<Message, Boolean> executeResponse;
		protected BiFunction<Message, Matcher, Boolean> executeMatcherResponse;

		public Builder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			this.regex = regex;
			this.name = name;
			this.userCategory = userCategory;
			this.channelCategory = channelCategory;
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

		public Reaction build() {
			if (executeResponse == null && executeMatcherResponse == null)
				throw new IllegalArgumentException("Must define execute");
			final Reaction reaction = new Reaction(name, getMatcher(regex), deactivated, userCategory, channelCategory);
			if (executeResponse == null)
				reaction.executeMatcherResponse = executeMatcherResponse;
			else
				reaction.executeResponse = executeResponse;
			return reaction;
		}

	}
}
