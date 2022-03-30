package com.wordpress.brancodes.test;

import com.mifmif.common.regex.Generex;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;

import java.util.Comparator;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
public class RegExTesting {

	public static void main(String[] args) {
		// System.out.println(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"));
		// new Generex(Commands.commands.get())
		// 		.getMatchedStrings(100)
		// 		.stream()
		// 		.sorted(Comparator.comparingInt(String::length))
		// 		.forEach(System.out::println);
	}

}
