package com.wordpress.brancodes.util;

import com.wordpress.brancodes.messaging.reactions.unit.BMI;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaUtil {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JavaUtil.class);

	public static <T> Set<T> arrayToSet(T[] arr) {
		// return Stream.of(arr).collect(Collectors.toSet());
		// return new HashSet<T>(Arrays.asList(array));
		// Stream.of(Arrays.stream(arr).boxed().toArray(Integer[]::new)).collect(Collectors.toSet());
		//
		Set<T> set = new HashSet<>();
		Collections.addAll(set, arr);
		return set;
	}

	public static Set<Long> longArrayToSet(long[] arr) {
		Set<Long> set = new HashSet<>();
		for (long l : arr)
			set.add(l);
		return set;
	}

	public static <T, R> R nullify(T val, Function<T, R> process) {
		return val == null ? null : process.apply(val);
	}

	/**
	 * example:
	 * <pre>
	 *     log(square(5));
	 *     return log(5 + log(square(14)));
	 * </pre>
	 */
	public static <T> T log(T t) {
		LOGGER.info(t.toString());
		return t;
	}

	public static <T> T log(T t, Logger logger) {
		logger.info(t.toString());
		return t;
	}

}
