package com.wordpress.brancodes.messaging.reactions.message.commands.custom.matching;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.TokenType;

import java.util.function.Function;

public abstract class Pattern<T, I, R> {

	T[] types;
	Function<I[], R> reduce;
	int presedence;
	boolean leftAssociativity;

	abstract boolean match(T[] types);

}
