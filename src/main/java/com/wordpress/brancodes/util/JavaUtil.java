package com.wordpress.brancodes.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaUtil {

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

}
