package com.wordpress.brancodes.messaging.reactions.commands.custom.types;

import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.TokenType;
import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.tree.TokenTypeType;
import org.apache.commons.collections4.set.UnmodifiableSet;

import java.util.Set;

public abstract class Type<T> implements Nameable, TokenType {
	String name;

	public Type(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract void set(Type<T> type);

	public class Instance {
		protected T real;

		public Instance(T real) {
			this.real = real;
		}

		public T get() {
			return real;
		}

		public Type<?>.Instance tryCast(Type<?> other) {
			return new VoidType().create();
		}

		public boolean exists() {
			return true;
		}

	}

	public abstract Instance create(T real);

	protected final static UnmodifiableSet<TokenTypeType> proceeedingTokens = (UnmodifiableSet<TokenTypeType>) UnmodifiableSet.unmodifiableSet(Set.of(
			TokenTypeType.CONTROL
	));

//	public Set<TokenTypeType> proceedingTokens() {
//		return null;
//	}

}
