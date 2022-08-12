package com.wordpress.brancodes.test.proxy;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class TesterMessage extends ReceivedMessage {

	private static final Logger LOGGER = LoggerFactory.getLogger(TesterMessage.class);

	private boolean logReply;

	public TesterMessage(String content, User author, Member member, boolean logReply) {
		super(0L, new EmptyMessageChannel(), MessageType.DEFAULT, null, false, false, false, content, null,
			  author, member, null, null, null, Collections.emptyList(), Collections.emptyList(),
			  Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0, null, null);
		this.logReply = logReply;
	}

	public TesterMessage(String content, boolean logReply) {
		this(content, null, null, logReply);
		this.logReply = logReply;
	}

	public TesterMessage(String content) {
		this(content, true);
	}

	@NotNull
	@Override
	public MessageAction reply(@NotNull final Message message) {
		if (logReply)
			LOGGER.info(message.getContentRaw());
		return new EmptyMessageAction();
	}

	@NotNull
	@Override
	public MessageAction reply(@NotNull final CharSequence charSequence) {
		if (logReply)
			LOGGER.info(String.valueOf(charSequence));
		return new EmptyMessageAction();
	}

	@NotNull
	@Override
	public Guild getGuild() {
		return ProxyGuild.getInstance();
	}

}
