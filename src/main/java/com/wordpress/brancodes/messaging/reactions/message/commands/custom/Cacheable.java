package com.wordpress.brancodes.messaging.reactions.message.commands.custom;

interface Cacheable<T> {

	boolean isCached();

	T getCache();

}
