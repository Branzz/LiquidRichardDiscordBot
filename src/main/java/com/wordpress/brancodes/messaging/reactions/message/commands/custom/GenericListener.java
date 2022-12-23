package com.wordpress.brancodes.messaging.reactions.message.commands.custom;

import com.wordpress.brancodes.messaging.reactions.Reaction;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GenericListener extends ListenerAdapter {

	public static final Map<String, Map<Long, Map<String, Reaction<Event>>>> eventReactions = new HashMap<>(); // <event, <guildId <name, command>>>
	public static final Map<String, Reaction<Event>> eventReactionsByName = new HashMap<>(); // <event, command>
	public static final long DMS = 1L; // GuildID for when event not necessarily in Guild

	/**
	 * @param guildID 1: not guild dependant
	 * @return if command didn't already exist
	 */
	public static boolean put(String event, Long guildID, String name, Reaction<Event> eventReaction) {
		boolean notPresent = eventReactionsByName.get(name) == null;
		eventReactionsByName.put(name, eventReaction);
		eventReactions.computeIfAbsent(event, e -> new HashMap<>())
					  .computeIfAbsent(guildID, g -> new HashMap<>())
					  .put(name, eventReaction);
		return notPresent;
	}

	public static Reaction<Event> get(String name) {
		return eventReactionsByName.get(name);
	}

	@Override public void onEmojiAdded(@NotNull EmojiAddedEvent event)							 { on("emojiAdded", event); }
	@Override public void onUserUpdateName(@NotNull UserUpdateNameEvent event)					 { on("userUpdateName", event); }
	@Override public void onUserUpdateDiscriminator(@NotNull UserUpdateDiscriminatorEvent event) { on("userUpdateDiscriminator", event); }
	@Override public void onUserTyping(@NotNull UserTypingEvent event)							 { on("userTyping", event); }
	@Override public void onUserActivityStart(@NotNull UserActivityStartEvent event)			 { on("userActivityStart", event, event.getGuild()); }
	@Override public void onUserActivityEnd(@NotNull UserActivityEndEvent event)				 { on("userActivityEnd", event, event.getGuild()); }
	@Override public void onMessageReceived(@NotNull MessageReceivedEvent event)				 { on("messageReceived", event, event.getGuild()); }
	@Override public void onMessageUpdate(@NotNull MessageUpdateEvent event)					 { on("messageUpdate", event, event.getGuild()); }
	@Override public void onMessageDelete(@NotNull MessageDeleteEvent event)					 { on("messageDelete", event, event.getGuild()); }
	@Override public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)			 { on("messageReactionAdd", event, event.getGuild()); }
	@Override public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event)	 { on("messageReactionRemove", event, event.getGuild()); }
	@Override public void onChannelCreate(@NotNull ChannelCreateEvent event)					 { on("channelCreate", event, event.getGuild()); }
	@Override public void onChannelDelete(@NotNull ChannelDeleteEvent event)					 { on("channelDelete", event, event.getGuild()); }
	@Override public void onGuildJoin(@NotNull GuildJoinEvent event)							 { on("guildJoin", event, event.getGuild()); }
	@Override public void onGuildLeave(@NotNull GuildLeaveEvent event)							 { on("guildLeave", event, event.getGuild()); }
	@Override public void onGuildBan(@NotNull GuildBanEvent event)								 { on("guildBan", event, event.getGuild()); }
	@Override public void onGuildUnban(@NotNull GuildUnbanEvent event)							 { on("guildUnban", event, event.getGuild()); }

	private void on(String eventName, Event event) {
		on(eventName, event, DMS);
	}

	private void on(String eventName, Event event, Guild guild) {
		on(eventName, event, guild.getIdLong());
	}

	private static void on(String eventName, Event event, long guildID) {
		eventReactions.get(eventName).get(guildID).values().forEach(eventReaction -> eventReaction.execute(event));
	}

}
