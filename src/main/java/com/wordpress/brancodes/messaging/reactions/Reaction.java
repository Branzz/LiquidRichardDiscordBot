package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.cooldown.CooldownPool;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import com.wordpress.brancodes.util.AbstractBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.RegEx;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.wordpress.brancodes.util.JavaUtil.longArrayToSet;

public abstract class Reaction<T> { // abstract away Message to generic in super class

	protected @RegEx String name;
	protected boolean deactivated; // TODO remove from master reactions map to keep map tree style consistent (?)
	protected UserCategoryType userCategoryType;
	protected ReactionChannelType channelCategory;
	protected boolean logging;
	protected Set<CooldownPool<T, ?>> cooldownPools;
	protected boolean guildFiltering;
	protected Set<Long> guildList;
	protected boolean whitelist;
	protected String docs;
	protected String[] examples;

	// TODO could use AbstractMathLibrary's modular LogicStatements
	//  to replace enforcement of super calls by ANDing each part to
	//  a master condition (and rank them by easiness of calculating (easier
	//  with just a list anyway??))
	@OverridingMethodsMustInvokeSuper
	protected boolean canExecute(T t) {
		return !isDeactivated() && !hasCooldown(t);
	}

	public abstract ReactionResponse execute(T t);

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

	public UserCategoryType getUserCategory() {
		return userCategoryType;
	}

	public ReactionChannelType getChannelType() {
		return channelCategory;
	}

	public boolean logging() {
		return logging;
	}

	protected boolean hasCooldown(T t) {
		return !cooldownPools.stream().allMatch(cooldownPool -> cooldownPool.checkConverted(t));
	}

	protected void addCooldowns(T t) {
		cooldownPools.forEach(pool -> pool.addConverted(t));
	}

	public boolean guildAllowed(long guildId) {
		return !guildFiltering || (whitelist == guildList.contains(guildId));
	}

	public String getDocs() {
		return docs;
	}

	protected EmbedBuilder getMessageEmbed() {
		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(name)
													  .setColor(Color.YELLOW)
													  .addField("User", userCategoryType.toString(), true)
													  .addField("Location", channelCategory.toString(), true);
		cooldownPools.forEach(pool -> embedBuilder.addField("Cooldown", pool.toString(), false));
		if (docs != null)
			embedBuilder.addField("Docs", docs, false);
		if (examples != null)
			Arrays.stream(examples).forEach(ex -> embedBuilder.addField("Example", ex, false));
		if (isDeactivated())
			embedBuilder.addField("Deactivated", "", false);
		return embedBuilder;
	}

	public MessageEmbed toFullString() {
		return getMessageEmbed().build();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Template inheriting class:
	 * <pre> // TODO new lines
	 * {@code
	 * public static final class ReactionBuilder extends Builder<Reaction, ReactionBuilder> {
	 *
	 * 	public ReactionBuilder(final String name, final UserCategory userCategory, final ReactionChannelType channelCategory) {
	 * 		super(name, userCategory, channelCategory);
	 * 	}
	 *
	 *  @Override public Command build() { super.build(); return object; }
	 * 	@Override protected Reaction createObject() { return new Reaction(); }
	 * 	@Override protected ReactionBuilder thisObject() { return this; }
	 * }
	 * }
	 * </pre>
	 */
	public static abstract class Builder<T extends Reaction, B extends Builder<T, B>> extends AbstractBuilder<T, B> {

		public Builder(String name, UserCategoryType userCategoryType, ReactionChannelType channelCategory) {
			if (name.length() > 16)
				throw new InvalidParameterException("name \"" + name + "\" must be 16 characters or less");
			object.name = name;
			object.userCategoryType = userCategoryType;
			object.channelCategory = channelCategory;
			object.logging = true;
			object.cooldownPools = new LinkedHashSet<>();
		}

		public B disableLogging() {
			object.logging = false;
			return thisObject;
		}

		public B deactivated() {
			object.deactivated = true;
			return thisObject;
		}

		public B whitelistGuilds(long... guildIDs) {
			if (object.guildFiltering)
				throw new InvalidParameterException("can't combine whitelist and blacklist");
			object.guildFiltering = true;
			object.whitelist = true;
			object.guildList = longArrayToSet(guildIDs);
			return thisObject;
		}

		public B blacklistGuilds(long... guildIDs) {
			if (object.guildFiltering)
				throw new InvalidParameterException("can't combine whitelist and blacklist");
			object.guildFiltering = true;
			object.whitelist = false;
			object.guildList = longArrayToSet(guildIDs);
			return thisObject;
		}

		public B usedEverywhereCooldown(long duration) {
			return addCooldown(new CooldownPool<>(duration, m -> null, Object.class));
		}

		protected B addCooldown(CooldownPool cooldownPool) {
			object.cooldownPools.add(cooldownPool);
			return thisObject;
		}

		protected B docs(String docs) {
			object.docs = docs;
			return thisObject;
		}

		protected B examples(String... examples) {
			object.examples = examples;
			return thisObject;
		}

		@Override
		public T build() {
			return object;
		}

	}

}
