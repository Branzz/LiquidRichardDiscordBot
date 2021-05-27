package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.Message;

@FunctionalInterface
public interface ExecuteResponse {

	void execute(final Message message);

}
