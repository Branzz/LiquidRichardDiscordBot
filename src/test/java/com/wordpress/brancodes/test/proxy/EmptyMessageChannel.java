package com.wordpress.brancodes.test.proxy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import org.jetbrains.annotations.NotNull;

public class EmptyMessageChannel implements MessageChannel, MessageChannelUnion {

	private ChannelType type;
	private TextChannel textChannel;

	public EmptyMessageChannel() {
		textChannel = new TextChannelImpl(0, ProxyGuild.getInstance());
		this.type = ChannelType.TEXT;
	}

	@Override public long getLatestMessageIdLong() { return 0; }
	@Override public boolean canTalk() { return false; }
	@NotNull @Override public String getName() { return null; }
	@NotNull @Override public ChannelType getType() { return type; }

	@NotNull
	@Override
	public JDA getJDA() {
		return ProxyJDA.getInstance();
	}

	@Override public AuditableRestAction<Void> delete() { return null; }
	@Override public long getIdLong() { return 0; }
	@NotNull @Override public PrivateChannel asPrivateChannel() { return null; }
	@NotNull @Override public TextChannel asTextChannel() { return textChannel; }
	@NotNull @Override public NewsChannel asNewsChannel() { return null; }
	@NotNull @Override public ThreadChannel asThreadChannel() { return null; }
	@NotNull @Override public VoiceChannel asVoiceChannel() { return null; }
	@NotNull @Override public IThreadContainer asThreadContainer() { return null; }
	@NotNull @Override public GuildMessageChannel asGuildMessageChannel() { return null; }

}
