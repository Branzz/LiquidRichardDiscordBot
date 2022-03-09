package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.Message;

import java.util.function.Consumer;

@FunctionalInterface
public interface ExecuteResponse extends Consumer<Message> {

	@Override
	void accept(final Message message);

}
