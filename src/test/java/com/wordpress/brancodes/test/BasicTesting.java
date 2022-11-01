package com.wordpress.brancodes.test;

import com.wordpress.brancodes.messaging.reactions.ReactionManager;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import static com.wordpress.brancodes.util.RegexUtil.getMatcher;
import static com.wordpress.brancodes.util.RegexUtil.getCommandRegex;
import static java.util.stream.Collectors.joining;

public class BasicTesting {

	public static void main(String[] args) {
		sortTest();
	}

	private static void commandHelp(String message) {
		final Matcher matcher = getMatcher(getCommandRegex("(Tell\\s+Me(\\s+What(\\s+Is)?)|Define)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?"),
										   message);
		matcher.matches();
		System.out.println(matcher.groupCount());
		for (int i = 0; i < matcher.groupCount(); i++) {
			System.out.println(i + " " + matcher.group(i));
		}
		String commandName = matcher.group(19);
		if (commandName == null)
			commandName = matcher.group(20);
		if (commandName == null) {
			System.out.println(("Searched command was in another group"));
			return;
		}
		System.out.println(commandName);
		System.out.println(ReactionManager.reactionsByName.get(commandName).getName());

	}

	private static String censorChainRegex(String[] words, String joiner) {
		return orChainRegex(Arrays.stream(words).map(w -> censorRegex(w, joiner)).toArray(String[]::new));
	}

	private static String censorChainRegex(String words, String joiner) {
		return orChainRegex(Arrays.stream(words.split("\\s*,\\s*")).map(w -> censorRegex(w,joiner)).toArray(String[]::new));
	}

	protected static String orChainRegex(String[] chain) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (String part: chain)
			sb.append(part)
			  .append('|');
		sb.delete(sb.length() - 1, sb.length());
		sb.append(')');
		return sb.toString();
	}

	private static String censorRegex(String word, String joiner) {
		return word.chars().mapToObj(c -> "[" + Character.toUpperCase((char) c) + Character.toLowerCase((char) c) + "]").collect(joining(joiner));
	}

	private static void sortTest() {
		class X {
			X(int id, boolean val) { this.id = id; this.val = val; }
			int id;
			boolean val;
			boolean getVal() { return val; }
		}
		List.of(new X(1, true), new X(2, true), new X(3, false), new X(4, false), new X(5, true)).stream()
			.sorted(Comparator.comparing(X::getVal))
			.map(x -> x.id)
			.forEachOrdered(System.out::println);
	}

	private static void funct(int square) {
		int i = 1;
		while (i * i < square)
			i++;
		System.out.println(i);
		int j = 1;
		while (j * j <= square)
			j++;
		System.out.println(j);

	}

	protected static int x;
}
