package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTreeNode;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTreeNodeFunctional;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTypeType;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTypeType.*;

public enum ControlToken implements TokenType, Symbolic {
	IF("if", ANY_TYPE, PRIM_TYPE, CODE), ELSE("else", ANY_TYPE, CODE),
	LEFT_PAREN("(", ANY), RIGHT_PAREN(")", CODE),
	LEFT_BRACKET("{", CODE), RIGHT_BRACKET("}", CODE),
	CALL(".", ANY_TYPE),
	DELIMITER(";", CODE, CODE),
	RETURN("return", ANY_TYPE, ANY_TYPE);

	private String[] symbols;
	private TokenTypeType returnType;
	private TokenTypeType[] childrenTypes;

	ControlToken(String... symbols) {
		this.symbols = symbols;
	}

	ControlToken(String symbol1, TokenTypeType returnType, TokenTypeType... childrenTypes) {
		this.symbols = getSymbols(symbol1);
		this.returnType = returnType;
		this.childrenTypes = childrenTypes;
	}

	private String[] getSymbols(String... symbols) {
		return symbols;
	}

	public String[] getSymbols() {
		return symbols;
	}

	public int childrenAmount() {
		return childrenTypes.length;
	}

	public TokenTreeNodeFunctional toNode(Supplier<TokenTreeNode> childrenSupplier) {
//		TokenTreeNode[] children = new TokenTreeNode[childenAmount()];
//		for (int i = 0; i < childenAmount(); i++)
//			children[i] = childrenSupplier.get();
//		TokenType[] children = IntStream.range(0, childenAmount())
//										 .mapToObj(i -> childrenSupplier.get())
//										 .toArray(TokenType[]::new);
		return new TokenTreeNodeFunctional(this, returnType,
				childrenTypes, Stream.generate(childrenSupplier)
					  .limit(childrenAmount())
					  .toArray(TokenTreeNode[]::new));
	}

}
