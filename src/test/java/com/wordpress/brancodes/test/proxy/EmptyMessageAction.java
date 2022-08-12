package com.wordpress.brancodes.test.proxy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EmptyMessageAction extends MessageActionImpl {

	public EmptyMessageAction(JDA api, String messageId, MessageChannel channel) {
		super(api, messageId, channel);
	}

	public EmptyMessageAction() {
		this(ProxyJDA.getInstance(), "0", new EmptyMessageChannel());
	}

	@Override
	public void queue() { }

	@Override
	public void queue(@Nullable final Consumer<? super Message> success) { }

	@Override
	public void queue(final Consumer<? super Message> success, final Consumer<? super Throwable> failure) { }

}
