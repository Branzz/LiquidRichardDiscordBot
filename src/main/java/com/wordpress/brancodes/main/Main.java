package com.wordpress.brancodes.main;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.messaging.reactions.Reactions;
import com.wordpress.brancodes.messaging.reactions.message.commands.SlashCommand;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		Runtime.getRuntime().addShutdownHook(new Thread(Reactions::flushAutoDeleteQueue));

		try (InputStreamReader iReader = new InputStreamReader(System.in);
			 BufferedReader bReader = new BufferedReader(iReader)) {

			/*
			 * Example inputs:
			 * Upsert help nickname NickName True
			 * upsert help commandthatdoesntexist falsity
			 */
			String[] input = bReader.readLine().split("\\s+");
			while (input[0].equalsIgnoreCase("upsert")) {
				List<String> inputStrings = Arrays.asList(input);
				Set<String> upsertCommands = new HashSet<>(inputStrings.subList(1, input.length - 1));
				Reactions.reactions.stream()
						.filter(r -> r instanceof SlashCommand)
						.filter(r -> upsertCommands.contains(r.getName()))
						.map(r -> (SlashCommand) r)
						.forEach(s -> s.upsert(inputStrings.size() > 2 && inputStrings.get(inputStrings.size() - 1).equalsIgnoreCase("true")));
				input = bReader.readLine().split("\\s+");
			}

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
