package com.wordpress.brancodes.util;

import com.mifmif.common.regex.Generex;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import java.util.Map;
import java.util.Random;

public class MyGenerex extends Generex {

	public MyGenerex(String regex) {
		super(regex);
	}

	//
	// public MyGenerex(Automaton automaton) {
	// 	this(automaton, new Random());
	// 	System.out.println("\f\r\t\n\b\1\2\3\4");
	// }
	//
	// public MyGenerex(String regex, Random random0) {
	// 	regex = requote(regex);
	// 	regExp = createRegExp(regex);
	// 	automaton = regExp.toAutomaton();
	// 	random = random0;
	// }
	//
	// public MyGenerex(Automaton automaton, Random random) {
	// 	super(automaton, random);
	// }
	//
	// private static RegExp createRegExp(String regex) {
	// 	String finalRegex = regex;
	// 	for (Map.Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()) {
	// 		finalRegex = finalRegex.replaceAll(charClass.getKey(), charClass.getValue());
	// 	}
	// 	return new RegExp(finalRegex);
	// }

}
