package com.wordpress.brancodes.test.proxy;

import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

public class ProxyGuild extends GuildImpl {

	private static final ProxyGuild INSTANCE = new ProxyGuild();

	private ProxyGuild() {
		super(ProxyJDA.getInstance(), 0);
	}

	public static ProxyGuild getInstance() {
		return INSTANCE;
	}

}
