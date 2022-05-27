package com.wordpress.brancodes.messaging.reactions.commands.custom;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.*;
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

public class CustomCommandListener extends ListenerAdapter {

	private static final Map<String, Map<Long, Map<String, CustomCommand>>> guildCommands = new HashMap<>(); // <event, <guildId <name, command>>>
	private static final Map<String, CustomCommand> commandsByName = new HashMap<>(); // <event, command>

	/**
	 * @param guildID 1: not guild dependant
	 * @return if command didn't already exist
	 */
	public static boolean put(String event, Long guildID, String name, CustomCommand customCommand) {
		boolean notPresent = commandsByName.get(name) == null;
		commandsByName.put(name, customCommand);
		// "UserUpdateNameEvent" || "UserUpdateDiscriminatorEvent"
		guildCommands.computeIfAbsent(event, e -> new HashMap<>())
					 .computeIfAbsent(guildID, g -> new HashMap<>())
					 .put(name, customCommand);
		return notPresent;
	}

	public static CustomCommand get(String name) {
		return commandsByName.get(name);
	}

	@Override public void onUserUpdateName(@NotNull UserUpdateNameEvent event) { on("UserUpdateNameEvent", event); }
	@Override public void onUserUpdateDiscriminator(@NotNull UserUpdateDiscriminatorEvent event) { on("UserUpdateDiscriminatorEvent", event); }
	@Override public void onUserTyping(@NotNull UserTypingEvent event) { if (event.getType().isGuild()) on("UserTypingEvent", event, event.getGuild()); }
	@Override public void onUserActivityStart(@NotNull UserActivityStartEvent event) { on("UserActivityStartEvent", event, event.getGuild()); }
	@Override public void onUserActivityEnd(@NotNull UserActivityEndEvent event) { on("UserActivityEndEvent", event, event.getGuild()); }
	@Override public void onMessageReceived(@NotNull MessageReceivedEvent event) { on("MessageReceivedEvent", event, event.getGuild()); }
	@Override public void onMessageUpdate(@NotNull MessageUpdateEvent event) { on("MessageUpdateEvent", event, event.getGuild()); }
	@Override public void onMessageDelete(@NotNull MessageDeleteEvent event) { on("MessageDeleteEvent", event, event.getGuild()); }
	@Override public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) { on("MessageReactionAddEvent", event, event.getGuild()); }
	@Override public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) { on("MessageReactionRemoveEvent", event, event.getGuild()); }
	@Override public void onChannelCreate(@NotNull ChannelCreateEvent event) { on("ChannelCreateEvent", event, event.getGuild()); }
	@Override public void onChannelDelete(@NotNull ChannelDeleteEvent event) { on("ChannelDeleteEvent", event, event.getGuild()); }
	@Override public void onGuildJoin(@NotNull GuildJoinEvent event) { on("GuildJoinEvent", event, event.getGuild()); }
	@Override public void onGuildLeave(@NotNull GuildLeaveEvent event) { on("GuildLeaveEvent", event, event.getGuild()); }
	@Override public void onGuildBan(@NotNull GuildBanEvent event) { on("GuildBanEvent", event, event.getGuild()); }
	@Override public void onGuildUnban(@NotNull GuildUnbanEvent event) { on("GuildUnbanEvent", event, event.getGuild()); }

	private void on(String eventName, Event event) {
		// allGuildcommands.get(eventName).values().forEach(customCommand -> customCommand.execute(event));
	}

	private void on(String eventName, Event event, Guild guild) {
		guildCommands.get(eventName).get(guild.getIdLong()).values().forEach(customCommand -> customCommand.execute(event));
	}

}
