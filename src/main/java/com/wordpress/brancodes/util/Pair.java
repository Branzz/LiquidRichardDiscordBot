package com.wordpress.brancodes.util;

import java.util.Map;

public class Pair<K, V> implements Map.Entry<K, V> {

	protected K key;
	protected V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public static <K, V> Pair<K, V> of(K key, V value) {
		return new Pair<>(key, value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	public K left() {
		return getKey();
	}

	public V right() {
		return getValue();
	}

	@Override
	public V setValue(final V value) {
		return this.value = value;
	}

	@Override
	public String toString() {
		return "(" + getKey() + ", " + getValue() + ")";
	}

}
