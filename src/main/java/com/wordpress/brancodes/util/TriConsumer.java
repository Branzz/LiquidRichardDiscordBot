package com.wordpress.brancodes.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface TriConsumer<T, U, V> extends Function<T, BiConsumer<U, V>> {

	public static void main(String[] args) {
		Function<Integer, BiConsumer<String, Character>> a = i -> (s, c) -> System.out.println((s + c).repeat(i));
		Function<Integer, Function<String, Consumer<Character>>> b = i -> s -> c -> System.out.println((s + c).repeat(i));
		TriConsumer<Integer, String, Character> x = (integer, s, character) -> System.out.println((s + character).repeat(integer));
		// equivalent ^
		BiFunction<Long, Character, String> y = (l, c) -> "a";
		Function<Long, Function<Character, String>> z = l -> c -> "abc";
	}

	void accept(T t, U u, V v);

	@Override
	default BiConsumer<U, V> apply(T t) {
		return null;
	}

	default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
		Objects.requireNonNull(after);

		return (l, r, v) -> {
			accept(l, r, v);
			after.accept(l, r, v);
		};
	}

}
