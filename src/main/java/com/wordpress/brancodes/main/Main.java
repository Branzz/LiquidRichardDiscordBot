package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.messaging.reactions.commands.Command;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;

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

		// System.out.println(Commands.commands.size());

		// for (Command command : Commands.commands)
		// 	 System.out.println(command.matcher.toString());

		// final String name = "Liquid Richard";
		// if (!bot.getJDA().getSelfUser().getName().equals(name))
		// 	bot.setName(name);

		// System.out.println(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"));

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
