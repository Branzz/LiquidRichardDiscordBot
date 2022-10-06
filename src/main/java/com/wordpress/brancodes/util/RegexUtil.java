package com.wordpress.brancodes.util;

import javax.annotation.RegEx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RegexUtil {

	public static final String apostrophes = "'\u2018\u2019";
	public static final String doubleQuotes = "\"\u201C\u201D\u2033";
	private static final Matcher breakCharMatcher = getMatcher("\\\\u([\\da-fA-F]{4})");
	private static Matcher charGroupSpecials = getMatcher("\\[.*(\\\\([sdDsSwW]).*)+\\]");

	//	private static String fixCharGroups(String regex) {
	//		charGroupSpecials.reset(regex).results().forEach(match -> replace(regex, match.start(), match.end(), extractToUnion(match)));
	//		return regex;
	//	}

	private static String anyEndsRegex(String regex) {
		return "[\\w\\W]*" + regex + "[\\w\\W]*";
	}

	public static String noLetterEndsRegex(String regex) {
		return "(?<![\\w]+)" + regex + "(?![\\w]+)";
	}

	public static String orChainRegex(String[] chain) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (String part : chain)
			sb.append(part)
					.append('|');
		sb.delete(sb.length() - 1, sb.length());
		sb.append(')');
		return sb.toString();
	}

	public static Collector<CharSequence, ?, String> orChainRegex() {
		return Collectors.joining("|", "(", ")");
	}

	public static Matcher getMatcher(@RegEx String regex, String input) {
		return getMatcherFlags(regex, input, Pattern.UNICODE_CHARACTER_CLASS);
	}

	public static Matcher getMatcherFlags(@RegEx String regex, String input, int flags) {
		return Pattern.compile(regex, flags).matcher(input);
	}

	public static Matcher getMatcherCaseInsensitive(@RegEx String regex) {
		return getMatcherFlags(regex, "", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);
	}

	public static Matcher getMatcher(@RegEx String regex) {
		return getMatcher(regex, "");
	}

}
