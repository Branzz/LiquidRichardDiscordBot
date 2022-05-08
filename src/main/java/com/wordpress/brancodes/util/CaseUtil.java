package com.wordpress.brancodes.util;

import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nullable;
import java.util.function.Consumer;
public final class CaseUtil {

	public static String properCase(String string) {
		StringBuilder properCase = new StringBuilder(string.length());
		for (String word : string.split("[^\\w\\d'.]")) {
			if (word.length() > 0) {
				boolean isNumber = true;
				double number = 0d;
				try {
					number = Double.parseDouble(word);
					if (number > Integer.MAX_VALUE) {
						if (number % 1 == 0)
							properCase.append("<@").append(word).append(">");
						else
							throw new NumberFormatException();
					}
					else
						properCase.append(properCaseOfNumber(NumberToText.numberToString(number)));
				} catch (NumberFormatException e) {
					isNumber = false;
				}
				if (!isNumber) {
					properCase.append(Character.toUpperCase(word.charAt(0)));
					if (word.length() >= 2)
						properCase.append(word.substring(1));
				}
				if (properCase.charAt(properCase.length() - 1) != ' ')
					properCase.append(" ");
			}
		}
		String properCaseString = properCase.toString().trim();
		// if (properCaseString.length() >= 1 && properCaseString.charAt(properCaseString.length() - 1) != '.')
		// 	properCaseString += ".";
		return properCaseString;
	}

	public static String properCaseExcludeNumbers(String string) {
		StringBuilder properCase = new StringBuilder(string.length());
		for (String word : string.split("[^\\w\\d'.]")) {
			if (word.length() > 0) {
				properCase.append(Character.toUpperCase(word.charAt(0)));
				if (word.length() >= 2)
					properCase.append(word.substring(1));
				if (properCase.charAt(properCase.length() - 1) != ' ')
					properCase.append(" ");
			}
		}
		String properCaseString = properCase.toString().trim();
		// if (properCaseString.length() >= 1 && properCaseString.charAt(properCaseString.length() - 1) != '.')
		// 	properCaseString += ".";
		return properCaseString;
	}

	private static String properCaseOfNumber(String string) {
		StringBuilder properCase = new StringBuilder(string.length());
		for (String word : string.split("[ .-]")) {
			if (word.length() >= 1)
				properCase.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() >= 2)
				properCase.append(word.substring(1));
			properCase.append(" ");
		}
		return properCase.toString();
	}

	public static String asEmoji(String name) {
		return new StringBuilder(2 + name.length()).append(':').append(name).append(":").toString();
	}

	/**
	 * SimpleClassName -> Simple Class Name
	 */
	public static String addSpacesToProper(String string) {
		final char[] chars = string.toCharArray();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(chars[0]);
		for (int i = 1; i < string.length(); i++) {
			if (Character.isLetter(chars[i]) && Character.isUpperCase(chars[i])
				&& !Character.isWhitespace(chars[i - 1])) {
				stringBuilder.append(' ');
			}
			stringBuilder.append(chars[i]);
		}
		return stringBuilder.toString();
	}
	// @FunctionalInterface
	// public interface BotAction <T> {
	// 	RestAction<T> botAct(BotUnit bot);
	// }
	//
	// /**
	//  * @param botAction	- get the REST action from the bot
	//  * @param success	- what to do with the REST action, if null, it will skip it and move to the next bot
	//  */
	// public static <T> void iterRecurse(BotAction<T> botAction, @Nullable Consumer<? super T> success) {
	// 	final Iterator<BotUnit> botIterator = bots.iterator();
	// 	iterateBots(botIterator, botAction, success);
	// }
	//
	// private static <T> void iterateBots(final Iterator<BotUnit> botIterator, BotAction<T> botAction, @Nullable Consumer<? super T> success) {
	// 	if (botIterator.hasNext()) {
	// 		final RestAction<T> botRestAction = botAction.botAct(botIterator.next());
	// 		if (botRestAction != null)
	// 			botRestAction.queue(r -> {
	// 				if (success != null)
	// 					success.accept(r);
	// 				iterateBots(botIterator, botAction, success);
	// 			});
	// 		else
	// 			iterateBots(botIterator, botAction, success);
	// 	}
	// }

}
