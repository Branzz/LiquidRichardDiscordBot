package com.wordpress.brancodes.messaging.chat;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class VariatedChat extends PeriodicChat {

	private static final Random random = new Random();
	private final long varianceRange;
	private long nextVariance;

	public VariatedChat(ExecuteChat chat) {
		this(chat, 86_400_000);
	}

	public VariatedChat(ExecuteChat chat, long period) {
		this(chat, period, period / 20);
		resetVariance();
	}

	public VariatedChat(ExecuteChat chat, long period, long varianceRange) {
		super(chat, period);
		this.varianceRange = varianceRange;
	}

	@Override
	public void chat() {
		super.chat();
		resetVariance();
	}

	private void resetVariance() {
		nextVariance = (long) (random.nextGaussian() * varianceRange);
	}

	public long getNextVariance() {
		resetVariance();
		return nextVariance;
	}

	@Override
	public void schedule(ScheduledExecutorService scheduler) {
		scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				chat();
				scheduler.schedule(this, getPeriod() + getNextVariance(), TimeUnit.MILLISECONDS);
			}
		}, getPeriod() + getNextVariance(), TimeUnit.MILLISECONDS);

	}

}
