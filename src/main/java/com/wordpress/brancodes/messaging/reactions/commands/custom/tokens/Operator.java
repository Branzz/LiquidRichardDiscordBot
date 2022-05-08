package com.wordpress.brancodes.messaging.reactions.commands.custom.tokens;

import com.wordpress.brancodes.messaging.reactions.commands.custom.types.Type;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.Method;

import java.util.function.BiFunction;

public enum Operator implements TokenType, Symbolic {
	NOT("!", "~", "num", (Boolean b, Type<?>.Instance[] args) -> !b, "bool"),
	AND("&", "&&", "num", (Boolean b, Type<?>.Instance[] args) -> b && (boolean) args[0].get(), "bool"),
	OR("|", "||", "~", "num", (Boolean b, Type<?>.Instance[] args) -> b || (boolean) args[0].get(), "bool"),
	EQUALS("==", "is", "bool", (Object o, Type<?>.Instance[] args) -> o.equals(args[0]), "any"),
	TYPED_EQUALS("===", "bool", (Object o, Type<?>.Instance[] args) -> o.equals(args[0]), "any"),
	NOT_EQUALS("!=", "bool", (Object o, Type<?>.Instance[] args) -> !o.equals(args[0]), "any"),
	NOT_TYPED_EQUALS("!==", "bool", (Object o, Type<?>.Instance[] args) -> !o.equals(args[0]), "any"),
	LESS("<", "bool", call((l, r) -> l < r), "num"),
	GREATER(">", "bool", call((l, r) -> l > r), "num"),
	LESS_EQUAL("<=", "bool", call((l, r) -> l <= r), "num"),
	GREATER_EQUAL(">=", "bool", call((l, r) -> l >= r), "num"),
//	CALL(".", "object", (Object o, Type<?>.Instance[] args) -> !o.equals(args[0]), "str")
	;
	private final String[] symbols;
	private Method[] methods;

	<T, R> Operator(String symbol,
			 String returnTypeStr, BiFunction<T, Type<?>.Instance[], R> call, String... parameterTypeNames) {
		this.symbols = getSymbols(name().toLowerCase(), symbol);
		methods = new Method[symbols.length];
		for (int i = 0; i < methods.length; i++) {
			methods[i] = new Method<>(symbols[i], returnTypeStr, call, parameterTypeNames);
		}
	}

	<T, R> Operator(String symbol1, String symbol2,
			 String returnTypeStr, BiFunction<T, Type<?>.Instance[], R> call, String... parameterTypeNames) {
		 this.symbols = getSymbols(name().toLowerCase(), symbol1, symbol2);
		 methods = new Method[symbols.length];
		 for (int i = 0; i < methods.length; i++) {
			 methods[i] = new Method<>(symbols[i], returnTypeStr, call, parameterTypeNames);
		 }
	}

	<T, R> Operator(String symbol1, String symbol2, String symbol3,
			 String returnTypeStr, BiFunction<T, Type<?>.Instance[], R> call, String... parameterTypeNames) {
		 this.symbols = getSymbols(name().toLowerCase(), symbol1, symbol2, symbol3);
		 methods = new Method[symbols.length];
		 for (int i = 0; i < methods.length; i++) {
			 methods[i] = new Method<>(symbols[i], returnTypeStr, call, parameterTypeNames);
		 }
	}

	private static BiFunction<Long, Type<?>.Instance[], Boolean> call(BiFunction<Long, Long, Boolean> compare) {
		return (Long num, Type<?>.Instance[] args) -> compare.apply(num, (long) args[0].get());
	}

//	CALL(".", ANY_TYPE, CLASS_TYPE, CLASS_TYPE),
//	NOT("!", "~", PRIM_TYPE, PRIM_TYPE),
//	EQUALS("equals", "==", "is", PRIM_TYPE, ANY_TYPE, ANY_TYPE),
//	NOT_EQUALS("!=", PRIM_TYPE, ANY_TYPE, ANY_TYPE),
//	LESS("<", "less", PRIM_TYPE, PRIM_TYPE, PRIM_TYPE),
//	GREATER(">", "greater", PRIM_TYPE, PRIM_TYPE, PRIM_TYPE),
//	LESS_EQUAL("<=", PRIM_TYPE, PRIM_TYPE, PRIM_TYPE),
//	GREATER_EQUAL(">=", PRIM_TYPE, PRIM_TYPE, PRIM_TYPE),
//	;
//	private final String[] symbols;
//	private TokenTypeType returnType;
//	private TokenTypeType[] children;
//	private Function<TokenType[], TokenType> execute;
//
//	Operator(String symbol1, Function<TokenType[], TokenType> execute,
//			 TokenTypeType returnType, TokenTypeType... children) {
//		this.symbols = getSymbols(symbol1);
//		this.returnType = returnType;
//		this.children = children;
//	}
//
//	Operator(String symbol1, String symbol2, Function<TokenType[], TokenType> execute,
//			 TokenTypeType returnType, TokenTypeType... children) {
//		this.symbols = getSymbols(symbol1, symbol2);
//		this.returnType = returnType;
//		this.children = children;
//	}
//
//	Operator(String symbol1, String symbol2, String symbol3, Function<TokenType[], TokenType> execute,
//			 TokenTypeType returnType, TokenTypeType... children) {
//		this.symbols = getSymbols(symbol1, symbol2, symbol3);
//		this.returnType = returnType;
//		this.children = children;
//	}
//
	private String[] getSymbols(String... symbols) {
		return symbols;
	}

	public String[] getSymbols() {
		return symbols;
	}

	public Method[] getMethods() {
		return methods;
	}
}
