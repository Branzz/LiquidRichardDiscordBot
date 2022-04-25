package com.wordpress.brancodes.messaging.reactions;

public class Cooldown {

	private final long timeOutEnd;

	// for self categorization
	public Cooldown(long timeOutEnd) {
		this.timeOutEnd = System.currentTimeMillis() + timeOutEnd;
	}

	/**
	 * @return true if timeout has ended
	 */
	public boolean timeoutEnded() {
		return System.currentTimeMillis() >= timeOutEnd;
	}

}
