package com.wordpress.brancodes.util;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
public final class Util {

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


}
