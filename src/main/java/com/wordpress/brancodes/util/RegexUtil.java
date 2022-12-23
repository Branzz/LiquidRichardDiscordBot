package com.wordpress.brancodes.util;

import com.wordpress.brancodes.bot.Config;

import javax.annotation.RegEx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RegexUtil {

	public static final String apostrophes = "'\u2018\u2019";
	public static final String doubleQuotes = "\"\u201C\u201D\u2033";
	public static final @RegEx String objectPronunsPart = "(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)";
	private static final Matcher breakCharMatcher = getMatcher("\\\\u([\\da-fA-F]{4})");
	private static final @RegEx String aliasesRegexPart = (String) Config.get("aliasesRegex");
	private static final @RegEx String questionRegexPart = "(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)*";
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

	private static final int DEFAULT_FLAGS = Pattern.UNICODE_CHARACTER_CLASS;

	public static Collector<CharSequence, ?, String> orChainRegex() {
		return Collectors.joining("|", "(", ")");
	}

	public static Matcher getMatcher(@RegEx String regex, String input) {
		return getMatcherFlags(regex, input, DEFAULT_FLAGS);
	}

	public static Matcher getMatcherFlags(@RegEx String regex, String input, int flags) {
		return Pattern.compile(regex, flags).matcher(input);
	}

	public static Matcher getMatcherCaseInsensitive(@RegEx String regex) {
		return getMatcherFlags(regex, "", DEFAULT_FLAGS | Pattern.CASE_INSENSITIVE);
	}

	public static Matcher getMatcher(@RegEx String regex) {
		return getMatcher(regex, "");
	}

	public static @RegEx String getCommandRegex(@RegEx String regexQuestion) {
		return getCommandRegex(regexQuestion, questionRegexPart);
	}

	public static @RegEx String getCommandRegex(@RegEx String regexQuestion, @RegEx String questionRegexPart) {
		return "(\\s*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s*(,|\\.+|!+|\\s)?\\s*)?(("
			   + aliasesRegexPart + "\\s*(\\?+|\\.+|,|!+)?\\s+" + questionRegexPart + regexQuestion
			   + ")|(" + questionRegexPart + regexQuestion + "\\s*,?\\s+" + aliasesRegexPart //"\\s*(,|\\.+|!+|\\s)?\\s*"
			   + "))(\\s+Please)*\\s*(\\?+|\\.+|,|!+)?\\s*(\\s+(Thanks|Thank\\s+You)\\s*(\\.+|!+)?)?\\s*)";
	}

}
