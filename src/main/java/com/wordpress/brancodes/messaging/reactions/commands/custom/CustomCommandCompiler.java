package com.wordpress.brancodes.messaging.reactions.commands.custom;

import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.*;
import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.tree.Expressions;
import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.tree.TokenTreeNode;
import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.tree.TokenTypeType;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.ClassType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.ControlToken.IF;
import static com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.Symbolic.symbolsOf;

public class CustomCommandCompiler {

	private static final boolean includeWhitespace = false;

	/**
	 * @param event camelCase String of event that only happens in this guild's parameters
	 *              "bot" for access to default jda actions which can be run only once
	 */
	public static TokenTreeNode compile(String code, String event) throws CustomCommandCompileErrorException {
		ClassType<?> eventType = CustomCommand.events.get(event);
		if (eventType == null)
			throw new CustomCommandCompileErrorException("event not supported / found");
//		try {
			return new TreeBuilder(tokenize(code)).nextExpressions();
//		} catch (ReturnInterruption returned) {
//			return returned.returnValue;
//		}
	}

	static class Warning {
		Token onToken;
		String message;

		public Warning(Token onToken, String message) {
			this.onToken = onToken;
			this.message = message;
		}

		@Override
		public String toString() {
			return "There May Be A " + message + " At Index " + onToken.from + " \"" + onToken.val + "\"";
		}

	}

	private static class TreeBuilder {

		private static class ReturnInterruption extends Throwable {

			private Expressions returnValue;

			public ReturnInterruption(Expressions returnValue) {
				super();
				this.returnValue = returnValue;
			}

		}
		private final List<Warning> warnings;
		private final List<Token> tokens;
		private int index;

		public TreeBuilder(List<Token> tokens) {
			this.warnings = new ArrayList<>();
			this.tokens = tokens;
			this.index = 0;
		}

//		private TokenTreeNode collapseTree() {
//		}

		private CustomCommandCompileErrorException unexpectedException(Token token) {
			return new CustomCommandCompileErrorException("Unexpected Token " + token.tokenType + " At " + token.from);
		}

		private Expressions nextExpressions() {
//			TokenPosition expecting;
			Expressions expressions = new Expressions();
			List<Token> localTokens = new ArrayList<>();
			while (true) {
				if (index >= tokens.size()) {
//					expressions.add(TokenTreeNode.END); // more of a linked list way
					break;
				}
				Token token = tokens.get(index++);
				if (token.tokenTypeType == TokenTypeType.CONTROL) { // == tokenType instanceof ControlToken
					ControlToken controlToken = ((ControlToken) token.tokenType);
					switch (controlToken) {
						case LEFT_PAREN:
						case LEFT_BRACKET:
							expressions.add(nextExpressions());
						case RIGHT_PAREN:
						case RIGHT_BRACKET:
							return expressions;
						case CALL:
//							Token last = getLast(localTokens);
							if (localTokens.isEmpty())
								throw unexpectedException(token);
							/* try to interpret last as Type, then */
							break;
						case DELIMITER:
							// construct expression (?) TODO check if localTokens has any?
							if (!localTokens.isEmpty())
								throw unexpectedException(token);
							continue;
						case RETURN:
//							throw new ReturnInterruption(nextExpressions());
						case ELSE:
							if (expressions.empty() || expressions.last().type() != IF)
								throw unexpectedException(token);
							/* fall through */
						case IF:
						default:
							expressions.add(controlToken.toNode(this::nextExpressions));
					}
				}
//				else if (token.tokenTypeType == TokenTypeType.OPERATOR) { }
				else {
					if (localTokens.isEmpty())
						;
					else {
						Token lastLocalToken = localTokens.get(localTokens.size() - 1);
//						new TokenTreeNodeFunctional()
					}
				}

			}
			return expressions;
		}

		private static <T> T getLast(List<T> list) {
			return list.isEmpty() ? null : list.get(list.size() - 1);
		}

	}

	private static final Map<String, TokenType> tokenTable =
			Stream.of(symbolsOf(Operator.class), symbolsOf(ControlToken.class))
					.flatMap(m -> m.entrySet().stream())
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

	public static List<Token> tokenize(String code) {
		code = code.trim();
		List<Token> tokens = new ArrayList<>();
		char[] chars = code.toCharArray();
		boolean inString = false;
		boolean whitespaceToken = false;
		int width = 0;
		boolean hasNewLine = false;
		for (int from = 0, to = 0; to < chars.length; to++) {
			char c = chars[to];
			boolean atQuote = c == '\'' || c == '\"';
			if (inString) {
				if (atQuote) {
					tokens.add(new Token(from + 1, to - 1, code.substring(from + 1, to), CustomCommand.getType("str")));
				}
				continue;
			} else if (atQuote) {
				inString = true;
				continue;
			}
			boolean atWhitespace;
			switch (c) {
				case '\t':
					width += 4;
					atWhitespace = true;
					break;
				case '\n':
				case '\r':
					width = 0;
					hasNewLine = true;
					atWhitespace = true;
					break;
				case ' ':
					width++;
					atWhitespace = true;
					break;
				default:
					atWhitespace = false;
			}
			if (!atWhitespace && whitespaceToken) { // space -> letter
				if (includeWhitespace)
					tokens.add(new Token(from, to, code.substring(from, to + 1), new Whitespace(width, hasNewLine)));
				width = 0;
				hasNewLine = false;
				whitespaceToken = false;
			} else if (atWhitespace && !whitespaceToken) { // letter -> space
				String substring = code.substring(from, to + 1);
				tokens.add(new Token(from, to, substring, getToken(substring)));
				whitespaceToken = true;
			}
		}
		return tokens;
	}

	private static TokenType getToken(String token) {
		return tokenTable.getOrDefault(token, new OtherToken(token));
	}

	public static class Token {
		int lineNumber; // TODO
		int from;
		int to;
		String val; // TODO redundant later on?
		TokenType tokenType;
		TokenTypeType tokenTypeType;
		CreatedToken createdToken;

		public Token(int from, int to, String val, TokenType tokenType) {
			this.from = from;
			this.to = to;
			this.val = val;
			this.tokenType = tokenType;
			tokenTypeType = TokenTypeType.of(tokenType);
		}

	}

	static class CreatedToken {

	}

	private static class Pair<K, V> implements Map.Entry<K, V> {
		private final K key;
		private V value;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			return this.value = value;
		}

	}

	/*
		tryCatchReturn((Supplier<Expressions>) () -> nextExpressions(),
			Map.of(ReturnInterruption.class,
					(Supplier<Expressions>) () -> { throw unexpectedException(token); })
	);

	 */
	public static void tryCatch(Runnable try0, Map<Class<Throwable>, Runnable> catchers) {
		tryCatch(try0, catchers, () -> {});
	}

	public static void tryCatch(Runnable try0, Map<Class<Throwable>, Runnable> catchers, Runnable onFail) {
		try {
			try0.run();
		} catch (Throwable potentialCatch) {
			Runnable onCatch = catchers.get(potentialCatch.getClass());
			if (onCatch != null)
				onCatch.run();
			else
				throw potentialCatch;
		}
		onFail.run();
	}

	public static <T> T tryCatchReturn(Supplier<T> try0, Map<Class<Throwable>, Supplier<T>> catchers) {
		try {
			return try0.get();
		} catch (Throwable potentialCatch) {
			Supplier<T> onCatch = catchers.get(potentialCatch.getClass());
			if (onCatch != null)
				return onCatch.get();
			else
				throw potentialCatch;
		}
	}

}
