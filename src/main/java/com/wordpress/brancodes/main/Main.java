package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.messaging.reactions.Censoring;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	private static LiquidRichardBot bot;

	public static void main(String... args) {

		// PoolConnection.begin();

		try {
			bot = new LiquidRichardBot();
		} catch (LoginException e) {
			e.printStackTrace();
		}

//		if (args.length > 1)
//			upsertCommands(new HashSet<>(List.of(args)));

		// Runtime.getRuntime().addShutdownHook(new Thread(Censoring::flushAutoDeleteQueue));

		DocsGenerator.generateMdDocs();

		try (InputStreamReader iReader = new InputStreamReader(System.in);
			 BufferedReader bReader = new BufferedReader(iReader)) {

			/*
			 * Example inputs:
			 * Upsert help nickname NickName True
			 * upsert help commandthatdoesntexist falsity
			 */
			String[] input = bReader.readLine().split("\\s+");
			Censoring.flushAutoDeleteQueue();
			reset();
//			while (!input[0].equals("end")) {
//				...
//				input = bReader.readLine().split("\\s+");
//			}

			// PoolConnection.end();

			System.exit(0);

		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public static LiquidRichardBot getBot() {
		return bot;
	}

	public static void reset() {
		bot.pause();
		bot.shutdownChatSchedulers();
		bot.getJDA().shutdownNow();
	}

}
