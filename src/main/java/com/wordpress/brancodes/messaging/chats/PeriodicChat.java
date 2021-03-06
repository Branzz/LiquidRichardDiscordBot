package com.wordpress.brancodes.messaging.chats;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.main.Main;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class PeriodicChat extends Chat {

	private static final Random random = new Random();

	protected final long period; // milliseconds

	public PeriodicChat(ExecuteChat chat) {
		this(chat, 86_400_000L); // 1 day
	}

	public PeriodicChat(ExecuteChat chat, long period) {
		this(chat, period, "Periodic Chat");
	}

	public PeriodicChat(ExecuteChat chat, String name) {
		this(chat, 86_400_000L, name);
	}

	public PeriodicChat(ExecuteChat chat, long period, String name) {
		super(chat, name);
		this.period = period;
	}

	public long getPeriod() {
		return period;
	}

	@Override
	public void schedule(ScheduledExecutorService scheduler, long guildID) {
		scheduler.scheduleWithFixedDelay(() -> chat(Main.getBot().getMainChannel(guildID)), (long) (random.nextDouble() * getPeriod()), getPeriod(), TimeUnit.MILLISECONDS);
	}

}
