package com.wordpress.brancodes.util;

import com.wordpress.brancodes.messaging.reactions.unit.BMI;
import org.slf4j.LoggerFactory;

import java.util.*;
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


	// (Character[]) Stream.of(IntStream.of('i'), apostrophes.chars(), doubleQuotes.chars())
	// 				   .flatMapToInt(s -> s)
	// 				   .mapToObj(i -> (char) i)
	// 				   .toArray(Character[]::new)

	/**
	 * @param aT must provide at least one T for type checking
	 * @param os array of T, T[], or Iterable<T>s
	 * @return flatmap to T
	 */
	public static <T> T[] deepArrayMerge(T aT, Object... os) {
		List<T> ts = new ArrayList<>();
		ts.add(aT);
		for (Object o : os) {
			try {
			 Iterable<T> tIter = (Iterable<T>) o;
			 tIter.forEach(ts::add);
			} catch (ClassCastException ignored1) {
			 try {
			  T[] tArr = (T[]) o;
			  ts.addAll(Arrays.asList(tArr));
			 } catch (ClassCastException ignored2) {
			  try {
			   T t = (T) o;
			   ts.add(t);
			  } catch (ClassCastException ignored3) {
			   throw new IllegalArgumentException("argument must have only T and T[]");
			  }
			}
		  }
		}
		return toArray(ts);
	}

	private static <T> T[] toArray(List<T> list) {
		T[] toR = (T[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size());
		for (int i = 0; i < list.size(); i++) {
			toR[i] = list.get(i);
		}
		return toR;
	}

}
