package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree;

import java.util.ArrayList;
import java.util.List;

public class Expressions extends TokenTreeNode {
	List<TokenTreeNode> expressions;

	public Expressions() {
		this.expressions = new ArrayList<>();
	}

	public void add(TokenTreeNode node) {
		expressions.add(node);
	}

	public Object get() { // TODO SHOULD it be the last line? or the last non-void expression
		int i = 0;
		for (; i < expressions.size() - 1; i++) {
			TokenTreeNode expression = expressions.get(i);
			if (expression instanceof TokenTreeNodeFunctional) {
				((TokenTreeNodeFunctional) expression).execute(); // return result voided
			}
		}
		TokenTreeNode expression = expressions.get(i);
		if (expression instanceof TokenTreeNodeFunctional) {
			return ((TokenTreeNodeFunctional) expression).execute();
		} else
			return expression;
	}

	/**
	 * it may be empty when a ControlToken requests nextExpression and there are none left, for example
	 */
	public boolean empty() {
		return expressions.isEmpty();
	}

	public TokenTreeNode last() {
		return empty() ? null : expressions.get(expressions.size() - 1);
	}

}
