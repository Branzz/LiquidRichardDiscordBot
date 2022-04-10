package com.wordpress.brancodes.messaging.reactions.commands.custom;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommandCompiler.Symbolic.symbolsOf;

public class CustomCommandCompiler {

	static Map<String, TokenType> tokenTable =
			Stream.of(symbolsOf(Operator.class), symbolsOf(ControlToken.class), CustomCommand.types)
					.flatMap(m -> m.entrySet().stream())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

	public static List<TokenType> tokenize(String code) {
		return null;
	}

	static class Token {
		int from;
		int to;
		String val;
		TokenType tokenType;

		public Token(int from, int to, String val) {
			this.from = from;
			this.to = to;
			this.val = val;
		}

	}

	static interface TokenType {

	}

	interface Symbolic {

		String[] getSymbols();

		static <T extends Symbolic> Map<String, T> symbolsOf(Class<T> enumClass) {
			return Arrays.stream(enumClass.getEnumConstants())
					.flatMap(o -> Arrays.stream(o.getSymbols()) // .distinct()
							.map(s -> new AbstractMap.SimpleEntry<>(s, o)))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}

	}

	enum Operator implements TokenType, Symbolic {
		CALL("."), LEFT_PAREN("("), RIGHT_PAREN(")"), LEFT_BRACKET("{"), RIGHT_BRACKET("}"),
		NOT("!", "~"), EQUALS("equals", "==", "is"), NOT_EQUALS("!="), LESS("<"), GREATER(">"),
		LESS_EQUAL("<="), GREATER_EQUAL(">="), QUOTE("\""), DELIMITER(";")
		;
		private String[] symbols;

		Operator(String... symbols) {
			this.symbols = symbols;
		}

		public String[] getSymbols() {
			return symbols;
		}

	}

	enum ControlToken implements TokenType, Symbolic {
		IF("if"), ELSE("else");

		private String[] symbols;

		ControlToken(String... symbols) {
			this.symbols = symbols;
		}

		public String[] getSymbols() {
			return symbols;
		}

	}

}
