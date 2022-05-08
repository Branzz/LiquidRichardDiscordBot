package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;

import javax.security.auth.login.LoginException;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;

public class Main {

	private static LiquidRichardBot bot;

	public static void main(String... args) {

		// PoolConnection.begin();

		try {
			bot = new LiquidRichardBot();
		} catch (LoginException e) {
			e.printStackTrace();
		}

		// PoolConnection.end();

	}

	public static LiquidRichardBot getBot() {
		return bot;
	}

	public static void restart() {
		bot.getJDA().shutdown();
		bot.shutdownChatSchedulers();
		main();
	}

}
