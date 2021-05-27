package com.wordpress.brancodes.messaging.chat;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class PeriodicChat extends Chat {

	private static final Random random = new Random();
	protected final long period; // milliseconds

	public PeriodicChat(ExecuteChat chat) {
		super(chat);
		period = 86_400_000; // 1 day
	}

	public PeriodicChat(ExecuteChat chat, long period) {
		super(chat);
		this.period = period;
	}

	public long getPeriod() {
		return period;
	}

	@Override
	public void schedule(ScheduledExecutorService scheduler) {
		scheduler.scheduleWithFixedDelay(this::chat, (long) (random.nextDouble() * getPeriod()), getPeriod(), TimeUnit.MILLISECONDS);
	}

}
