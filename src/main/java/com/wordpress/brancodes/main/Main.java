package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;

import javax.security.auth.login.LoginException;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;

public class Main {

	private static LiquidRichardBot bot;

	// DAILY REMINDER
	// TODO create a todo command that loads into disk or a note command that saves
	// TODO make ERROR logging exception output in disc log channel be multi line up to 2000 chars instead
	// TODO case insensitive regex char
	public static void main(String... args) {

		// PoolConnection.begin();
		// TODO CONVERT TO SPRING DATA BASE

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
