package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import net.dv8tion.jda.api.events.Event;

public class EventReaction<E extends Event> extends Reaction<E> {

	protected EventReaction() { }

	@Override
	public ReactionResponse execute(E e) {
		return null;
	}

	public static abstract class Builder<EE extends Event, T extends EventReaction<EE>, B extends Builder<EE, T, B>> extends Reaction.Builder<T, B> {

		public Builder(String name) {
			super(name, UserCategoryType.DEFAULT, ReactionChannelType.GUILD);
		}

		@Override
		public T build() {
			return super.build();
		}

	}

	public static final class EventReactionBuilder<EE extends Event> extends Builder<EE, EventReaction<EE>, EventReactionBuilder<EE>> {

		public EventReactionBuilder(String name) {
			super(name);
		}

		@Override public EventReaction<EE> build() { return object; }
		@Override protected EventReaction<EE> createObject() { return new EventReaction<>(); }
		@Override protected EventReactionBuilder<EE> thisObject() { return this; }

	}

}
