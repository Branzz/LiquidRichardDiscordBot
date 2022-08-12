package com.wordpress.brancodes.test.proxy;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;

public class ProxyJDA extends JDAImpl {

	private static final ProxyJDA INSTANCE = new ProxyJDA();

	public static ProxyJDA getInstance() {
		return INSTANCE;
	}
	private ProxyJDA() {
		super(new AuthorizationConfig(""));
	}

	@Override
	public boolean isIntent(final GatewayIntent intent) {
		return true;
	}

}
