package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.messaging.reactions.commands.Command;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

import java.util.logging.Logger;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;

public class Main {

	private static LiquidRichardBot bot;

	// DAILY REMINDER
	// TODO create a todo command that loads into disk or a note command that saves
	// TODO UPDATE README file for command.builder
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
