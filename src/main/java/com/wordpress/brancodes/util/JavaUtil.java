package com.wordpress.brancodes.util;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.wordpress.brancodes.util.JDAUtil.IDMatcher;
import static java.util.stream.Collectors.toList;

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

	private static String substring(String string, int fromEndIndex) {
		return string.substring(0, string.length() - fromEndIndex);
	}

	public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

	public static String timeStampOf(final OffsetDateTime date) {
		return dateTimeFormatter.format(date);
	}

	public static String truncate(String text) {
		return truncateMiddle(text, 2000);
	}

	public static String truncate(String[] words, int maxSize) { // failure
		int joinedLength = Arrays.stream(words).mapToInt(String::length).sum() + (words.length - 1) * 2;
		if (joinedLength <= maxSize)
			return String.join(", ", words); // TODO truncate this just in case size is wrong? it shouldn't
		List<String> wordsByLength = Arrays.stream(words)
										   .sorted(Comparator.comparing(String::length).reversed())
										   .collect(toList());
		int totalLength = joinedLength;
		int ind = 0;
		while (totalLength > maxSize) {
			int wordLength = wordsByLength.get(ind).length();
			if (wordLength < 7)
				return truncateMiddle(String.join(", ", words), maxSize);
			/*
			totalLength aaaxxxxx
			wordLength  aaa
			take out between
			(x {1 to wL+7}) >= tL - maxSize
			dif = tL - maxSize;
			if (dif > wL+7)
			 */
			wordsByLength.set(ind, truncateMiddle(wordsByLength.get(ind), wordLength - maxSize));
			totalLength -= wordLength + 7;
			ind++;
		}
		return truncateEnd(String.join(", ", wordsByLength), maxSize);
	}

	public static String truncateMiddle(String text, int maxSize) {
		return text.length() > maxSize ? text.substring(0, maxSize / 2 - 1) + "..." + text.substring(text.length() - maxSize / 2 - 2) : text;
	}

	public static String truncateEnd(String text, int maxSize) {
		return text.length() > maxSize ? text.substring(0, maxSize - 3) + "..." : text;
	}

	/**
	 * @param tag the name of a server, channel, category, message, role, user, or emoji
	 * @param <T>
	 * @return
	 */
	public static <T extends ISnowflake> T tryGet(String tag, Function<String, T> getByID, RestAction<T> retrieveByID,
												  Function<String, T> getByName) {
		if (IDMatcher.reset(tag).matches()) {
			return Optional.ofNullable(getByID.apply(tag))
					.or(() -> Optional.ofNullable(retrieveByID.complete()))
					.orElse(getByName.apply(tag));
		} else {
			return getByName.apply(tag);
		}
	}

	/**
	 * {@link Optional#ifPresentOrElse(Consumer, Runnable)} with a return type
	 */
	public static <T, R> R presentOrElseReturn(Optional<T> optional, Function<? super T, R> action, Supplier<R> emptySupplier) {
		if (optional.isPresent()) {
			return action.apply(optional.get());
		} else {
			return emptySupplier.get();
		}
	}

	public static <T> boolean presentOrElseReturnStatus(Optional<T> optional, Consumer<? super T> action) {
		return presentOrElseReturn(optional, t -> { action.accept(t); return true; }, () -> false);
	}

	public static boolean booleanReturnStatus(boolean present, Runnable action) {
		if (present) {
			action.run();
			return true;
		} else {
			return false;
		}
	}

}
