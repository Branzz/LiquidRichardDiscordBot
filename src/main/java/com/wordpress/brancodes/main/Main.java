package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import net.dv8tion.jda.api.entities.GuildChannel;

import javax.security.auth.login.LoginException;
import java.util.stream.Collectors;

public class Main {

	private static LiquidRichardBot bot;

	public static void main(String[] args) throws LoginException, InterruptedException {

		// PoolConnection.begin();

		bot = new LiquidRichardBot();

		// PoolConnection.end();

	}

	public static LiquidRichardBot getBot() {
		return bot;
	}

}
