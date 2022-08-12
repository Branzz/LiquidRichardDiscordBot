package com.wordpress.brancodes.messaging.reactions;

import com.mifmif.common.regex.Generex;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
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
import java.util.regex.Pattern;

import static com.wordpress.brancodes.messaging.reactions.Reactions.truncateEnd;

public class MessageReaction extends Reaction {

	Matcher matcher;
	Function<Message, ReactionResponse> executeResponse;
	BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse;
	String generexString;
	Generex generex; // lazily created

	protected MessageReaction() { }

	public final boolean matches(String match) {
		return matcher.reset(match).results().findAny().isPresent();
	}

	public ReactionResponse execute(Message message) {
		return execute(message, message.getContentRaw());
	}

	public ReactionResponse execute(Message message, String match) {
		if (deactivated)
			return ReactionResponse.FAILURE;
		if (matches(match)) {
			ReactionResponse reactionResponse = accept(message);
			if (reactionResponse.status())
				addCooldowns(message);
			return reactionResponse;
		}
		return ReactionResponse.FAILURE;
	}

	protected ReactionResponse accept(Message message) {
		if (hasCooldown(message))
			return ReactionResponse.FAILURE;
		if (executeResponse != null)
			return executeResponse.apply(message);
		else
			return executeMatcherResponse.apply(message, matcher);
	}

	public String getRegex() {
		return matcher.pattern().pattern();
	}

	protected static String truncateField(String text) {
		return truncateEnd(text, 1024);
	}

	/**
	 * for children classes to add on to its toFullString embed builder
	 * <pre>
	 * \@Override
	 * public MessageEmbed toFullString() {
	 *     return getMessageEmbed.addField("MyProperty", property.toString(), true).build();
	 * }
	 * </pre>
	 */
	protected EmbedBuilder getMessageEmbed() {
		final EmbedBuilder embedBuilder =
				new EmbedBuilder().setTitle(name)
								  .setColor(Color.YELLOW)
								  .addField("RegEx", truncateField(matcher.pattern().toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\*", "\\\\*")), true)
								  .addField("User", userCategory.toString(), true)
								  .addField("Location", channelCategory.toString(), true);
		cooldownPools.forEach(pool -> embedBuilder.addField("Cooldown", pool.toString(), false));
		return embedBuilder;
	}

	public MessageEmbed toFullString() {
		return getMessageEmbed().build();
	}

	public static Matcher getMatcher(@RegEx String regex, String input) {
		return getMatcherFlags(regex, input, Pattern.UNICODE_CHARACTER_CLASS);
	}

	public static Matcher getMatcherFlags(@RegEx String regex, String input, int flags) {
		return Pattern.compile(regex, flags).matcher(input);
	}

	public static Matcher getMatcherCaseInsensitive(@RegEx String regex) {
		return getMatcherFlags(regex, "", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);
	}

	public static Matcher getMatcher(@RegEx String regex) {
		return getMatcher(regex, "");
	}

	public Generex getGenerex() {
		if (generex == null)
			generex = new Generex(generexString == null ? getRegex() : generexString);
		return generex;
	}

	public static abstract class Builder<T extends MessageReaction, B extends Builder<T, B>> extends Reaction.Builder<T, B> {

		protected final @RegEx String regex;
		protected boolean caseInsensitive = false;

		public Builder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, userCategory, channelCategory);
			this.regex = regex;
		}

		public B caseInsensitive() {
			caseInsensitive = true;
			return thisObject;
		}

		public B deactivated() {
			object.deactivated = true;
			return thisObject;
		}

		public B execute(Consumer<Message> executeResponse) {
			object.executeResponse = m -> {
				executeResponse.accept(m);
				return ReactionResponse.SUCCESS;
			};
			return thisObject;
		}

		public B execute(BiConsumer<Message, Matcher> executeMatcherResponse) {
			object.executeMatcherResponse = (m, r) -> {
				executeMatcherResponse.accept(m, r);
				return ReactionResponse.SUCCESS;
			};
			return thisObject;
		}

		// TODO untested
		public B execute(BiFunction<Message, Matcher, Boolean> passes, BiConsumer<Message, Matcher> executeMatcherResponse) {
			object.executeMatcherResponse = (m, r) -> {
				if (passes.apply(m, r)) {
					executeMatcherResponse.accept(m, r);
					return ReactionResponse.SUCCESS;
				} else {
					return ReactionResponse.FAILURE;
				}
			};
			return thisObject;
		}

		public B executeStatus(Function<Message, Boolean> executeResponse) {
			object.executeResponse = message -> new ReactionResponse(executeResponse.apply(message));
			return thisObject;
		}

		public B executeStatus(BiFunction<Message, Matcher, Boolean> executeMatcherResponse) {
			executeResponse((message, matcher) -> new ReactionResponse(executeMatcherResponse.apply(message, matcher)));
			return thisObject;
		}

		public B executeResponse(Function<Message, ReactionResponse> executeResponse) {
			object.executeResponse = executeResponse;
			return thisObject;
		}

		public B executeResponse(BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse) {
			object.executeMatcherResponse = executeMatcherResponse;
			return thisObject;
		}

		/**
		 * @param regex a RegEx String that works with Generex library (exclude < and >'s)
		 */
		public B generexString(String regex) {
			object.generexString = regex;
			return thisObject;
		}

		@Override
		public T build() {
			if (object.executeResponse == null && object.executeMatcherResponse == null)
				throw new IllegalArgumentException("Must define execute");
			if (object.generexString != null) {
				autoCreateGenerexString();
			}
			object.matcher = caseInsensitive ? getMatcherCaseInsensitive(regex) : getMatcher(regex);
			return object;
		}

		private void autoCreateGenerexString() {
			int lastLeftParenInd = -1; // TODO
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < regex.length(); i++) {
				if (regex.charAt(i) == '(')
					lastLeftParenInd = i;
			}
			object.generexString = str.toString();
		}

	}

	public static final class MessageReactionBuilder extends Builder<MessageReaction, MessageReactionBuilder> {

		public MessageReactionBuilder(final String name, final String regex, final UserCategory userCategory, final ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}

		@Override
		public MessageReaction build() {
			super.build();
			// if (object.executeResponse != null && object.executeMatcherResponse != null)
			// 	throw new IllegalArgumentException("Must define execute only once");
			return object;
		}
		@Override protected MessageReaction createObject() { return new MessageReaction(); }
		@Override protected MessageReactionBuilder thisObject() { return this; }

	}

}
