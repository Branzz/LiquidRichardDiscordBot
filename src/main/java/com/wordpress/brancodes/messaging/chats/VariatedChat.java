package com.wordpress.brancodes.messaging.chats;

import com.wordpress.brancodes.main.Main;
import net.dv8tion.jda.api.entities.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VariatedChat extends PeriodicChat {

	private static final Logger LOGGER = LoggerFactory.getLogger(VariatedChat.class);
	private static final Random random = new Random();

	private final long varianceRange;
	private long nextVariance;

	public VariatedChat(ExecuteChat chat) {
		this(chat, 86_400_000);
	}

	public VariatedChat(ExecuteChat chat, long period) {
		this(chat, period, period / 20);
	}

	public VariatedChat(ExecuteChat chat, long period, long varianceRange) {
		this(chat, period, varianceRange, "Variated Chat");
	}

	public VariatedChat(ExecuteChat chat, String name) {
		this(chat, 86_400_000, name);
	}

	public VariatedChat(ExecuteChat chat, long period, String name) {
		this(chat, period, period / 20, name);
	}

	public VariatedChat(ExecuteChat chat, long period, long varianceRange, String name) {
		super(chat, period, name);
		this.varianceRange = varianceRange;
		resetVariance();
	}

	@Override
	public void chat(Channel channel) {
		super.chat(channel);
		resetVariance();
	}

	private long resetVariance() {
		return nextVariance = (long) (random.nextGaussian() * varianceRange);
	}

	@Override
	public void schedule(ScheduledExecutorService scheduler, long guildID) {
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				chat(Main.getBot().getMainChannel(guildID));
				scheduler.schedule(this, getNextDelay(), TimeUnit.MILLISECONDS);
			}
		}, getCustomDelay(), TimeUnit.MILLISECONDS);

	}

	/**
	 * Log wrapper
	 */
	private long getNextDelay() {
		long delay = getPeriod() + resetVariance();
		LOGGER.info(name + " scheduled for: " + new Date(System.currentTimeMillis() + delay));
		return delay;
	}

	private long getCustomDelay() {
		return getNextDelay();
		// Date next = new Date(121, Calendar.AUGUST, 13, 18, 1, 1);

			// Date current = new Date();
			// Date next = new Date(121, current.getMonth(), current.getDate(), 20, 0, 0);
			// LOGGER.info(name + " scheduled for: " + next);
			// return next.getTime() - new Date().getTime();

		// return current.getTime() - new Date().getTime() + 5000;
	}

}
