package com.wordpress.brancodes.messaging.chat;

import java.util.concurrent.ScheduledExecutorService;
public abstract class Chat {

	protected ExecuteChat chat;

	public Chat(ExecuteChat chat) {
		this.chat = chat;
	}

	@FunctionalInterface
	protected interface ExecuteChat {
		void execute();
	}

	public void chat() {
		chat.execute();
	}

	public abstract void schedule(final ScheduledExecutorService scheduler);

}
