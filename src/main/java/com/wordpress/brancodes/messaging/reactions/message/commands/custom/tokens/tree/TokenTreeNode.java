package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.TokenType;

public abstract class TokenTreeNode {
	protected TokenType type;
	public TokenType type() {
		return type;
	}
//	abstract void execute();

	public static final TokenTreeNode END = new TokenTreeNode() {};

}
