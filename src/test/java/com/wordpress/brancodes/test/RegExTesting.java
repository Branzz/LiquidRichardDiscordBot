package com.wordpress.brancodes.test;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
public class RegExTesting {

	public static void main(String[] args) {
		System.out.println(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"));
		// new Generex(getCommandRegex("Help(\\s(Me|Him|Her|Them|It|Every(\\sOne)?)(\\sOut)?)?(\\sHere)?(\\s+Right\\sNow)?"))
		// 		.getMatchedStrings(50)
		// 		.stream()
		// 		.sorted(Comparator.comparingInt(String::length))
		// 		.forEach(System.out::println);
	}

}
