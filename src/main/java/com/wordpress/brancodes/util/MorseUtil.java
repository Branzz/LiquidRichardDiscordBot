package com.wordpress.brancodes.util;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MorseUtil {

	public static String toMorse(String message) {
		return message.chars()
					  .map(Character::toLowerCase)
					  .collect(StringBuilder::new, (sb, ch) -> {
					  	String key = morseCharsMap.getKey((char) ch);
					  	if (key != null)
					  		sb.append(key).append(" ");
					  	}, StringBuilder::append).toString();
	}

	public static String fromMorse(String morse) {
		return Arrays.stream(morse.split("\\s+"))
					 .collect(StringBuilder::new, (sb, key) -> {
					 	Character ch = morseCharsMap.get(key);
					 	if (ch != null)
							sb.append(ch);
						}, StringBuilder::append).toString();
	}

	public static Matcher morseMatcher = Pattern.compile("\\s*(([.-]+|/)\\s+)*([.-]+|/)\\s*").matcher("");

	public static boolean isMorse(String string) {
		return morseMatcher.reset(string).matches();
	}

	private final static BidiMap<String, Character> morseCharsMap = new TreeBidiMap<>();
	// private final static PatriciaTrie<Character> morseChars = new PatriciaTrie<>(morseCharsMap);

	static {
		morseCharsMap.put("/",		' ');
		morseCharsMap.put(".-",	 	'a');
		morseCharsMap.put("-...",	'b');
		morseCharsMap.put("-.-.",	'c');
		morseCharsMap.put("-..",	'd');
		morseCharsMap.put(".",		'e');
		morseCharsMap.put("..-.",	'f');
		morseCharsMap.put("--.",	'g');
		morseCharsMap.put("....",	'h');
		morseCharsMap.put("..",	 	'i');
		morseCharsMap.put(".---",	'j');
		morseCharsMap.put("-.-",	'k');
		morseCharsMap.put(".-..",	'l');
		morseCharsMap.put("--",		'm');
		morseCharsMap.put("-.",		'n');
		morseCharsMap.put("---",	'o');
		morseCharsMap.put(".--.",	'p');
		morseCharsMap.put("--.-",	'q');
		morseCharsMap.put(".-.",	'r');
		morseCharsMap.put("...",	's');
		morseCharsMap.put("-",		't');
		morseCharsMap.put("..-",	'u');
		morseCharsMap.put("...-",	'v');
		morseCharsMap.put(".--",	'w');
		morseCharsMap.put("-..-",	'x');
		morseCharsMap.put("-.--",	'y');
		morseCharsMap.put("--..",	'z');
		morseCharsMap.put("-----",	'0');
		morseCharsMap.put(".----",	'1');
		morseCharsMap.put("..---",	'2');
		morseCharsMap.put("...--",	'3');
		morseCharsMap.put("....-",	'4');
		morseCharsMap.put(".....",	'5');
		morseCharsMap.put("-....",	'6');
		morseCharsMap.put("--...",	'7');
		morseCharsMap.put("---..",	'8');
		morseCharsMap.put("----.",	'9');
		morseCharsMap.put(".-.-.-", '.');
		morseCharsMap.put("--..--", ',');
		morseCharsMap.put("..--..", '?');
		morseCharsMap.put("---...", ':');
		morseCharsMap.put(".----.", '\'');
		morseCharsMap.put(".--.-.", '@');
	}

}
