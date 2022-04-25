package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Two potential models:
 * One huge pool of Cooldowns that are checked one by one or -- save space
 * A Map OR Queue (ordered by time) system with Guild/Channel keys; variant: one-to-one with Reaction -- save time, more complex to implement
 */
public class CooldownManager {

	public CooldownManager(boolean createGuildCooldownPool, boolean createChannelCooldownPool, boolean createMemberCooldownPool, boolean createDMCooldownPool) {
			guildCooldownPool = createGuildCooldownPool ? new HashMap<>() : null;
			channelCooldownPool = createChannelCooldownPool ? new HashMap<>() : null;
			memberCooldownPool = createMemberCooldownPool ? new HashMap<>() : null;
			DMCooldownPool = createDMCooldownPool ? new HashMap<>() : null;
	}

	public CooldownManager() {
		guildCooldownPool = new HashMap<>();
		channelCooldownPool = new HashMap<>();
		memberCooldownPool = new HashMap<>();
		DMCooldownPool = new HashMap<>();
	}

	private final Map<Guild, Cooldown> guildCooldownPool;
	private final Map<TextChannel, Cooldown> channelCooldownPool;
	private final Map<Member, Cooldown> memberCooldownPool;
	private final Map<PrivateChannel, Cooldown> DMCooldownPool;

	private long guildCooldown = 0L;
	private long channelCooldown = 0L;
	private long memberCooldown = 0L;
	private long DMCooldown = 0L;

	/**
	 * @return whether this can be executed
	 */
	public boolean check(Message message) {
		if (message.isFromGuild()) {
			return (check(guildCooldownPool, message.getGuild())
				   && check(channelCooldownPool, message.getTextChannel())
				   && check(memberCooldownPool, message.getMember()));
		} else {
			return check(DMCooldownPool, message.getPrivateChannel());
		}
	}

	private static <T, M extends Map<T, Cooldown>> boolean check(M cooldownMap, T locker) {
		Cooldown cooldown = cooldownMap.get(locker);
		if (cooldown == null)
			return true;
		if (cooldown.timeoutEnded()) {
			cooldownMap.remove(locker);
			return true;
		}
		return false;
	}

}
