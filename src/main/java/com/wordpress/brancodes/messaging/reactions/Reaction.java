package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.cooldown.CooldownPool;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.RegEx;
import java.security.InvalidParameterException;
import java.util.LinkedHashSet;
import java.util.Set;

public class Reaction {

	protected @RegEx String name;
	boolean deactivated; // TODO remove from master reactions map to keep map tree style consistent (?)
	UserCategory userCategory;
	ReactionChannelType channelCategory;
	Set<CooldownPool<?>> cooldownPools;

	protected boolean hasCooldown(final Message message) {
		return !cooldownPools.stream().allMatch(cooldownPool -> cooldownPool.check(message));
	}

	protected void addCooldowns(Message message) {
		cooldownPools.forEach(pool -> pool.add(message));
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

	public ReactionChannelType getChannelType() {
		return channelCategory;
	}

	public UserCategory getUserCategory() {
		return userCategory;
	}

	public static abstract class Builder<T extends Reaction, B extends Builder<T, B>> extends AbstractBuilder<T, B> {

		public Builder(String name, UserCategory userCategory, ReactionChannelType channelCategory) {
			if (name.length() > 16)
				throw new InvalidParameterException("name \"" + name + "\" must be 16 characters or less");
			object.name = name;
			object.userCategory = userCategory;
			object.channelCategory = channelCategory;
			object.cooldownPools = new LinkedHashSet<>();
		}

		public B deactivated() {
			object.deactivated = true;
			return thisObject;
		}

		public B addGuildCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild", new CooldownPool<>(duration, Message::getGuild, Guild.class));
		}

		public B addChannelCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild text channel", new CooldownPool<>(duration, m -> m.getChannel().asTextChannel(), TextChannel.class));
		}

		public B addMemberCooldown(long duration) {
			return addCooldown(ChannelType.TEXT, "Guild member", new CooldownPool<>(duration, Message::getMember, Member.class));
		}

		public B addDMCooldown(long duration) {
			return addCooldown(ChannelType.PRIVATE, "DM", new CooldownPool<>(duration, m -> m.getChannel().asPrivateChannel(), PrivateChannel.class));
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
			return object;
		}

	}

	public static final class ReactionBuilder extends Builder<Reaction, ReactionBuilder> {

		public ReactionBuilder(final String name, final UserCategory userCategory, final ReactionChannelType channelCategory) {
			super(name, userCategory, channelCategory);
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
