package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.cooldown.CooldownPool;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.RegEx;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reaction {

	protected @RegEx String name;
	Matcher matcher;
	boolean deactivated; // TODO remove from master reactions map to keep map tree style consistent (?)
	UserCategory userCategory;
	ReactionChannelType channelCategory;
	Function<Message, ReactionResponse> executeResponse;
	BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse;
	Set<CooldownPool<?>> cooldownPools;

	protected Reaction() { }

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

	protected boolean hasCooldown(final Message message) {
		return !cooldownPools.stream().allMatch(cooldownPool -> cooldownPool.check(message));
	}

	protected void addCooldowns(Message message) {
		cooldownPools.forEach(pool -> pool.add(message));
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
								  .addField("RegEx", matcher.pattern().toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\*", "\\\\*"), true)
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

	public static abstract class Builder<T extends Reaction, B extends Builder<T, B>> extends AbstractBuilder<T, B> {

		protected final @RegEx String regex;
		protected boolean caseInsensitive = false;

		public Builder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			if (name.length() > 16)
				throw new InvalidParameterException("name \"" + name + "\" must be 16 characters or less");
			this.regex = regex;
			object.name = name;
			object.userCategory = userCategory;
			object.channelCategory = channelCategory;
			object.cooldownPools = new LinkedHashSet<>();
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

		public B addGuildCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild", new CooldownPool<>(duration, Message::getGuild, Guild.class));
		}

		public B addChannelCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild text channel", new CooldownPool<>(duration, Message::getTextChannel, TextChannel.class));
		}

		public B addMemberCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild member", new CooldownPool<>(duration, Message::getMember, Member.class));
		}

		public B addDMCooldown(long duration) {
			return addCooldown(ChannelType.PRIVATE, "DM", new CooldownPool<>(duration, Message::getPrivateChannel, PrivateChannel.class));
		}

		private B addCooldown(ChannelType intendedLocation, String locationName, CooldownPool cooldownPool) {
			if (!object.channelCategory.inRange(intendedLocation))
				throw new InvalidParameterException(locationName + "cooldowns can't be used in " + object.channelCategory);
			return addCooldown(cooldownPool);
		}

		private B addCooldown(CooldownPool cooldownPool) {
			object.cooldownPools.add(cooldownPool);
			return thisObject;
		}

		/**
		 * if users are sending the exact same message
		 */
		public B addContentCooldown(long duration) {
			object.cooldownPools.add(new CooldownPool<>(duration, Message::getContentRaw, String.class));
			return thisObject;
		}

		@Override
		public T build() {
			if (object.executeResponse == null && object.executeMatcherResponse == null)
				throw new IllegalArgumentException("Must define execute");
			object.matcher = caseInsensitive ? getMatcherCaseInsensitive(regex) : getMatcher(regex);
			return object;
		}

	}

	public static final class ReactionBuilder extends Builder<Reaction, ReactionBuilder> {

		public ReactionBuilder(final String name, final String regex, final UserCategory userCategory, final ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}

		@Override
		public Reaction build() {
			super.build();
			// if (object.executeResponse != null && object.executeMatcherResponse != null)
			// 	throw new IllegalArgumentException("Must define execute only once");
			return object;
		}
		@Override protected Reaction createObject() { return new Reaction(); }
		@Override protected ReactionBuilder thisObject() { return this; }

	}

}
