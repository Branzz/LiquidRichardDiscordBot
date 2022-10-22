package com.wordpress.brancodes.messaging.reactions.message;

import com.mifmif.common.regex.Generex;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.cooldown.CooldownPool;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import com.wordpress.brancodes.util.RegexUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.RegEx;
import java.security.InvalidParameterException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;

import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.FAILURE;
import static com.wordpress.brancodes.util.JavaUtil.truncateEnd;

public class MessageReaction extends Reaction<Message> {

	Matcher matcher;
	Function<Message, ReactionResponse> executeResponse;
	BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse;
	String generexString;
	Generex generex; // lazily created

	protected MessageReaction() { }

	public final boolean matches(String match) {
		return matcher.reset(match).results().findAny().isPresent();
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	protected boolean canExecute(Message message) {
		return !isDeactivated()
			   && ((message.getChannelType() != null && !message.isFromGuild()) || (message.getGuild() != null && guildAllowed(message.getGuild().getIdLong())))
			   && matches(message.getContentRaw())
			   && super.canExecute(message);
	}

	public ReactionResponse execute(Message message) {
		if (canExecute(message)) {
			ReactionResponse reactionResponse = accept(message);
			if (reactionResponse.status())
				addCooldowns(message);
			return reactionResponse;
		} else {
			return FAILURE;
		}
	}

	protected ReactionResponse accept(Message message) { // TODO refactor this method away?
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
		return super.getMessageEmbed().addField("RegEx", truncateField(matcher.pattern()
																			  .toString()
																			  .replaceAll("\\\\", "\\\\\\\\")
																			  .replaceAll("\\*", "\\\\*")), true);
	}

	public Generex getGenerex() {
		if (generex == null) {
			try {
				generex = new Generex(generexString == null ? getRegex() : generexString);
			} catch (IllegalArgumentException e) {
				generex = new Generex("The Author Of The Generex Library Abandoned Us!");
			}
		}
		return generex;
	}

	public static abstract class Builder<T extends MessageReaction, B extends Builder<T, B>> extends Reaction.Builder<T, B> {

		protected final @RegEx String regex;
		protected boolean caseInsensitive = false;
		protected boolean reactsPositive;

		Function<Message, ReactionResponse> preExecuteResponse;
		BiFunction<Message, Matcher, ReactionResponse> preExecuteMatcherResponse;

		public Builder(String name, @RegEx String regex, UserCategoryType userCategoryType, ReactionChannelType channelCategory) {
			super(name, userCategoryType, channelCategory);
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
			preExecuteResponse = m -> {
				executeResponse.accept(m);
				return ReactionResponse.SUCCESS;
			};
			return thisObject;
		}

		public B execute(BiConsumer<Message, Matcher> executeMatcherResponse) {
			preExecuteMatcherResponse = (m, r) -> {
				executeMatcherResponse.accept(m, r);
				return ReactionResponse.SUCCESS;
			};
			return thisObject;
		}

		// TODO untested
		public B execute(BiFunction<Message, Matcher, Boolean> passes, BiConsumer<Message, Matcher> executeMatcherResponse) {
			preExecuteMatcherResponse = (m, r) -> {
				if (passes.apply(m, r)) {
					executeMatcherResponse.accept(m, r);
					return ReactionResponse.SUCCESS;
				} else {
					return FAILURE;
				}
			};
			return thisObject;
		}

		public B executeStatus(Function<Message, Boolean> executeResponse) {
			preExecuteResponse = message -> new ReactionResponse(executeResponse.apply(message));
			return thisObject;
		}

		public B executeStatus(BiFunction<Message, Matcher, Boolean> executeMatcherResponse) {
			executeResponse((message, matcher) -> new ReactionResponse(executeMatcherResponse.apply(message, matcher)));
			return thisObject;
		}

		public B executeResponse(Function<Message, ReactionResponse> executeResponse) {
			preExecuteResponse = executeResponse;
			return thisObject;
		}

		public B executeResponse(BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse) {
			preExecuteMatcherResponse = executeMatcherResponse;
			return thisObject;
		}

		public B emojiReaction(Emoji emoji) {
			// TODO
			return thisObject;
		}

		/**
		 * @param regex a RegEx String that works with Generex library (exclude < and >'s)
		 */
		public B generexString(String regex) {
			object.generexString = regex;
			return thisObject;
		}

		public B addGuildCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild", new CooldownPool<>(duration, Message::getGuild, Guild.class));
		}

		public B addMessageChannelCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild text channel", new CooldownPool<>(duration, Message::getChannel, MessageChannel.class));
		}

		public B addMemberCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild member", new CooldownPool<>(duration, Message::getMember, Member.class));
		}

		public B addDMCooldown(long duration) {
			return addCooldown(ChannelType.PRIVATE, "DM", new CooldownPool<>(duration, m -> m.getChannel().asPrivateChannel(), PrivateChannel.class));
		}

		protected B addCooldown(ChannelType intendedLocation, String locationName, CooldownPool<Message, ?> cooldownPool) {
			if (!object.channelCategory.inRange(intendedLocation))
				throw new InvalidParameterException(locationName + "cooldowns can't be used in " + object.channelCategory);
			return addCooldown(cooldownPool);
		}

		/**
		 * if users are sending the exact same message
		 */
		public B addContentCooldown(long duration) {
			object.cooldownPools.add(new CooldownPool<>(duration, Message::getContentRaw, String.class));
			return thisObject;
		}

		public B andReactPositive() {
			reactsPositive = true;
			return thisObject;
		}

		@Override
		public T build() {
			if (preExecuteResponse == null && preExecuteMatcherResponse == null)
				throw new IllegalArgumentException("Must define execute");
			if (object.generexString != null) {
				autoCreateGenerexString();
			}
			if (preExecuteResponse != null) {
				object.executeResponse = reactsPositive ? message -> {
					ReactionResponse reactionResponse = preExecuteResponse.apply(message);
					if (reactionResponse.status()) {
						PreparedMessages.reply(message, "positive");
					}
					return reactionResponse;
				} : preExecuteResponse;
			} else {
				object.executeMatcherResponse = reactsPositive ? (message, matcher) -> {
					ReactionResponse reactionResponse = preExecuteMatcherResponse.apply(message, matcher);
					if (reactionResponse.status()) {
						PreparedMessages.reply(message, "positive");
					}
					return reactionResponse;
				} : preExecuteMatcherResponse;
			}
			object.matcher = caseInsensitive ? RegexUtil.getMatcherCaseInsensitive(regex) : RegexUtil.getMatcher(regex);
			return object;
		}

		private void autoCreateGenerexString() {
			// int lastLeftParenInd = -1; // TODO
			// StringBuilder str = new StringBuilder();
			// for (int i = 0; i < regex.length(); i++) {
			// 	if (regex.charAt(i) == '(')
			// 		lastLeftParenInd = i;
			// }
			// object.generexString = str.toString();
		}

	}

	public static final class MessageReactionBuilder extends Builder<MessageReaction, MessageReactionBuilder> {

		public MessageReactionBuilder(String name, @RegEx String regex, UserCategoryType userCategoryType, ReactionChannelType channelCategory) {
			super(name, regex, userCategoryType, channelCategory);
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
