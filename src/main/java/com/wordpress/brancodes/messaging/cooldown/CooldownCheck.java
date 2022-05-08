package com.wordpress.brancodes.messaging.cooldown;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.function.Function;

public class CooldownCheck extends Cooldown {

	private final Function<Message, Boolean> cooldownChecker;

	/**
	 * @param timeOutEnd duration in milliseconds
	 */
	public CooldownCheck(long timeOutEnd, Function<Message, Boolean> cooldownChecker) {
		super(timeOutEnd);
		this.cooldownChecker = cooldownChecker;
	}
	public CooldownCheck(long timeOutEnd, MessageChannel channel) {
		this(timeOutEnd, message -> message.getChannel().getIdLong() == channel.getIdLong());
	}

	public CooldownCheck(long timeOutEnd, Member member) {
		this(timeOutEnd, message -> message.getMember() != null && message.getMember().getIdLong() == member.getIdLong());
	}

	public boolean check(Message message) {
		return cooldownChecker.apply(message);
	}

}
