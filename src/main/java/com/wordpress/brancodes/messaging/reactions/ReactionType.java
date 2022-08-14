package com.wordpress.brancodes.messaging.reactions;

public enum ReactionType {
	MessageReaction(com.wordpress.brancodes.messaging.reactions.message.MessageReaction.class),
	SlashCommand(com.wordpress.brancodes.messaging.reactions.message.commands.SlashCommand.class);

	private final Class<? extends Reaction> associatedClass;

	ReactionType(Class<? extends Reaction> associatedClass) {
		this.associatedClass = associatedClass;
	}

	public Class<? extends Reaction> associatedClass() {
		return associatedClass;
	}

}
