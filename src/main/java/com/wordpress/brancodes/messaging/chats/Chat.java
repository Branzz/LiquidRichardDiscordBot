package com.wordpress.brancodes.messaging.chats;

import java.util.concurrent.ScheduledExecutorService;

public abstract class Chat {

	protected final String name;
	protected final ExecuteChat chat;

	public Chat(ExecuteChat chat) {
		this.chat = chat;
		this.name = "Chat";
	}

	public Chat(ExecuteChat chat, String name) {
		this.chat = chat;
		this.name = name;
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
