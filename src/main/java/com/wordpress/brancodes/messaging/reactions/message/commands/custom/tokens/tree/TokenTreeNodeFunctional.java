package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.TokenType;

public class TokenTreeNodeFunctional extends TokenTreeNode {
	private TokenTypeType returnType;
	private TokenTypeType[] expectedChildrenTypes;
	private TokenTreeNode[] children;

	public TokenTreeNodeFunctional(TokenType tokenType, TokenTypeType returnType, TokenTypeType[] expectedChildrenTypes, TokenTreeNode[] children) {
		this.type = tokenType;
		this.returnType = returnType;
		this.expectedChildrenTypes = expectedChildrenTypes;
		this.children = children;
	}

	public Object execute() {
		return null;
	}
}
