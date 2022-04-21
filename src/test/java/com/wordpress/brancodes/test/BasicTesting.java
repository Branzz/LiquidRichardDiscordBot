package com.wordpress.brancodes.test;

import com.wordpress.brancodes.messaging.reactions.Reactions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import static com.wordpress.brancodes.messaging.reactions.Reaction.getMatcher;
import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
import static java.util.stream.Collectors.joining;
public class BasicTesting {

	public static void main(String[] args) {
		funct(5 * 5 - 1);
		funct(5 * 5);
		funct(46340 * 46340 - 1);
		funct(46340 * 46340);
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
