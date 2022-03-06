package com.wordpress.brancodes.util;

import javax.annotation.RegEx;

public class QuestionsUtil {

	public static final @RegEx String questionRegex = "";

	static {
		final String[] modalVerbs = { "can", "could", "may", "might", "shall", "should", "will", "would", "must" };
		final String[] subjectPronouns = { "i", "you", "he", "she", "it", "one", "we", "you all", "they" }; // and a name itself (user @)
		// final String[]
	}
}
