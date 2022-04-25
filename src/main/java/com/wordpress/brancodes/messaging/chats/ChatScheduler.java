package com.wordpress.brancodes.messaging.chats;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ChatScheduler {

	private static final transient Random random = new Random();

	private final ScheduledExecutorService scheduler;
	private long guildID;
	private final Chats chats;

	public ChatScheduler(Chats chats, int corePoolSize, long guildID) {
		this.chats = chats;
		scheduler = new ScheduledThreadPoolExecutor(corePoolSize);
		this.guildID = guildID;
		scheduleChats();
	}

	public ChatScheduler(Chats chats, long guildID) {
		this(chats, chats.getChats().size(), guildID);
	}

	private void scheduleChats() {
		chats.getChats().forEach(chat -> chat.schedule(scheduler, guildID));
	}

	public void setMainChannel(TextChannel mainChannel) {
		chats.setMainChannel(mainChannel);
	}

	public void shutdown() {
		scheduler.shutdown();
	}

}
