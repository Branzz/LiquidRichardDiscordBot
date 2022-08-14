package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public interface Symbolic {

	String[] getSymbols();

	static <T extends Symbolic> Map<String, T> symbolsOf(Class<T> enumClass) {
		return Arrays.stream(enumClass.getEnumConstants())
				.flatMap(o -> Arrays.stream(o.getSymbols()) // .distinct()
						.map(s -> new AbstractMap.SimpleEntry<>(s, o)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
