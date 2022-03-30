package com.wordpress.brancodes.test;

import com.mifmif.common.regex.Generex;

import java.util.Comparator;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
public class RegExTesting {

	public static void main(String[] args) {
		// System.out.println(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"));
		new Generex("(Th(at|is) )?User (Was|Is)( Not|n't)( A)? (Mod|Moderator)\\.?")
				.getMatchedStrings(100)
				.stream()
				.sorted(Comparator.comparingInt(String::length))
				.forEach(System.out::println);

		Generex generex = new Generex("abc([.!]|\\s)*");
		for (int i = 0; i <= 10; i++) {
			System.out.println("S" + generex.random() + "E");
		}

	}

}
