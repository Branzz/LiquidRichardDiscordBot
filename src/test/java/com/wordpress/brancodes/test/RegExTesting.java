package com.wordpress.brancodes.test;

import com.mifmif.common.regex.Generex;
import com.mifmif.common.regex.util.Iterator;

import java.util.Comparator;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
public class RegExTesting {

	public static void main(String[] args) {
		// System.out.println(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"));

		Generex generex = new Generex("abc([.!]|\\s)*");
		for (int i = 0; i <= 10; i++) {
			System.out.println("S" + generex.random() + "E");
		}

	}

}
