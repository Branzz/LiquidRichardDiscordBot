package com.wordpress.brancodes.messaging.chats;

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
	public void chat() {
		super.chat();
		resetVariance();
	}

	private long resetVariance() {
		return nextVariance = (long) (random.nextGaussian() * varianceRange);
	}

	@Override
	public void schedule(ScheduledExecutorService scheduler) {
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				chat();
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
		// return getNextDelay();
		// Date next = new Date(121, Calendar.AUGUST, 13, 18, 1, 1);
		Date current = new Date();
		Date next = new Date(121, current.getMonth(), current.getDate(), 18, 15, 0);
		LOGGER.info(name + " scheduled for: " + next);
		return next.getTime() - new Date().getTime();
		// return current.getTime() - new Date().getTime() + 5000;
	}

}
