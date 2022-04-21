package com.wordpress.brancodes.messaging.reactions;

public abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> {

	public AbstractBuilder() {
		this.object = createObject();
		this.thisObject = thisObject();
	}

	protected T object;
	protected B thisObject;
	protected abstract T createObject(); // handled by the leaves
	protected abstract B thisObject(); // handled by the leaves
	public abstract T build();

}
