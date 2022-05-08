package com.wordpress.brancodes.messaging.reactions.commands.custom.types;

public interface Cast<T, I> {
	T tryCast(I inst);
}
