package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.ControlToken;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.Operator;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.OtherToken;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.TokenType;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.PrimitiveType;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.Type;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;

import javax.annotation.Nonnull;

public enum TokenTypeType { // order by most specific
	PRIM_TYPE(PrimitiveType.class), CLASS_TYPE(ClassType.class), ANY_TYPE(Type.class),
	CONTROL(ControlToken.class),
	OPERATOR(Operator.class),
	ANY(TokenType.class),
	OTHER(OtherToken.class),
	CODE(null);

	private Class<? extends TokenType> tokenTypeClass;

	TokenTypeType(Class<? extends TokenType> tokenTypeClass) {
		this.tokenTypeClass = tokenTypeClass;
	}

	@Nonnull
	public static TokenTypeType of(TokenType tokenType) {
		for (TokenTypeType value : values()) {
			if (value.tokenTypeClass.equals(tokenType.getClass()))
				return value;
		}
		/* unreachable */
		return ANY;
	}

}
