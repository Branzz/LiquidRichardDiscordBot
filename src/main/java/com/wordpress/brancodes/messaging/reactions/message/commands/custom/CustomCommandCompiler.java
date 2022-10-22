package com.wordpress.brancodes.messaging.reactions.message.commands.custom;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.*;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.Expressions;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTreeNode;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTypeType;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.ControlToken.IF;
import static com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.Symbolic.symbolsOf;

public class CustomCommandCompiler {

	private static final boolean includeWhitespace = false;

	/**
	 * @param event camelCase String of event that only happens in this guild's parameters
	 *              "bot" for access to default jda actions which can be run only once
	 */
	public static TokenTreeNode compile(String code, String event) throws CustomCommandCompileErrorException {
		return compile(code, CustomCommand.events.get(event));
	}

	public static TokenTreeNode compile(String code, ClassType<?> eventType) throws CustomCommandCompileErrorException {
//		try {
		if (eventType == null)
			throw new CustomCommandCompileErrorException("event not supported / found");

		return new TreeBuilder(tokenize(code)).nextExpressions();
//		} catch (ReturnInterruption returned) {
//			return returned.returnValue;
//		}
	}

	public static String compileAndRun(String code, String event) {
		try {
			compile(code, event).run();
			return "Ran";
		} catch (CustomCommandCompileErrorException e) {
			return e.getMessage();
		}
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

		public static void main(String[] args) {

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
		boolean inSymbols = false;
		int lastSymbol = -1;
		boolean whitespaceToken = false;
		int width = 0;
		boolean hasNewLine = false;
		int lineNumber = 0;
		for (int from = 0, to = 0; to < chars.length; to++) {
			char c = chars[to];
			boolean atQuote = c == '\'' || c == '\"';
			boolean atSymbol = !Character.isLetter(c);
			if (inString) {
				if (atQuote) {
					tokens.add(new Token(lineNumber, from + 1, to - 1, code.substring(from + 1, to), CustomCommand.getType("str")));
					from = to + 1;
					inString = false;
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
					lineNumber++;
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
			if (!atWhitespace && whitespaceToken) { // space -> _
				if (includeWhitespace) {
					tokens.add(new Token(lineNumber, from, to, code.substring(from, to + 1), new Whitespace(width, hasNewLine)));
					from = to + 1;
				}
				width = 0;
				hasNewLine = false;
				whitespaceToken = false;
			} else if (atWhitespace && !whitespaceToken) { // _ -> space
				String substring = code.substring(from, to);
				tokens.add(new Token(lineNumber, from, to, substring, getToken(substring)));
				from = to + 1;
				whitespaceToken = true;
			}
			if (!atSymbol && inSymbols) { // symbol -> letter // TODO should be to _ (with space)
				final String tokenString = code.substring(lastSymbol, to);
				Token token = new Token(lineNumber, lastSymbol, to, tokenString, getToken(tokenString));
				if (token.tokenTypeType != TokenTypeType.OTHER) { // while loop, enclosing space
					tokens.add(new Token(lineNumber, from, lastSymbol, code.substring(from, lastSymbol)));
					tokens.add(token);
					from = to;
				}
				inSymbols = false;
			} else if (atSymbol && !inSymbols) { // letter -> symbol
				lastSymbol = to;
				inSymbols = true;
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

		public Token(int lineNumber, int from, int to, String val, TokenType tokenType) {
			this.lineNumber = lineNumber;
			this.from = from;
			this.to = to;
			this.val = val;
			this.tokenType = tokenType;
			tokenTypeType = TokenTypeType.of(tokenType);
		}

		public Token(int lineNumber, int from, int to, String val) {
			this(lineNumber, from, to, val, getToken(val));
		}

		@Override
		public String toString() {
			return "line " + lineNumber + " '" + val + "' from " + from + " to " + to + " " + tokenType + " " + tokenTypeType + " createdToken=" + createdToken;
		}

	}

	static class CreatedToken {

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
