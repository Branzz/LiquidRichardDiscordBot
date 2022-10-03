package com.wordpress.brancodes.test;

import bran.tree.compositions.Definition;
import bran.tree.compositions.expressions.LimitExpression;
import bran.tree.compositions.expressions.values.Constant;
import bran.tree.compositions.expressions.values.Variable;
import bran.tree.compositions.sets.regular.SpecialSetType;
import com.mifmif.common.regex.Generex;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bran.tree.compositions.statements.Statement.forAll;

public class General {

	//  0110100110010110100101100110100110010110011010010110100110010110100101100110100101101001100101100110
//		0	1	1	0	1	0	0	1	1	0	0	1	0	1	1	0	1	0	0	1	0	1	1	0	0

	enum X {
		A(), B;
		X() {
			class C {
				class D {}
			}
		}
		void m() {}
		public static int Z = 1;
		public static class i {}
	}
	interface Y {
		int Z = 0;
		void m();
		public static class i {}
	}
	abstract class Z {
		public abstract void m();
		public class i {}
	}

	public static void main(String[] args) {
		char[] arr = {'a','c','e'};
		new String(arr).chars().mapToObj(i -> (Character) (char) i);

				// return forAll(Variable.of("\u03B5")).in(SpecialSetType.R).itHolds(e ->
				// 	e.greater(Constant.ZERO)
				//  	 .and(thereExists(Variable.of("\u03B4")).in(SpecialSetType.R).suchThat(d->
				// 				d.greater(Constant.ZERO).and(
				// 						forAll(Variable.of("x")).in(SpecialSetType.R).itHolds(x ->
				// 						Constant.ZERO.less(ABS.ofS(x.minus(limitExp.approaching())))
				// 									 .and(ABS.ofS(x.minus(limitExp.approaching())).less(d))
				// 								.implies(ABS.ofS(limitExp.function().minus(limitExp.approached())).less(e))
				// 					).proven())
				// 			).proven())
				// ).proven();

		// System.out.println(Definition.LIMIT.of(((LimitExpression)
		// 	Variable.of("x").approaches(Constant.ONE).of(Variable.of("x").squared()))));

		// String doubleRegex = "[+-]?((((\\d+)(\\.)?((\\d+)?)" +
		// 		"([eE][+-]?(\\d+))?)|(\\.(\\d+)([eE][+-]?(\\d+))?)|" +
		// 		"(((0[xX]([0123456789abcdefABCDEF]+)(\\.)?)|(0[xX]([0123456789abcdefABCDEF]+)?(\\.)([0123456789abcdefABCDEF]+)))" +
		// 		"[pP][+-]?(\\d+)))[fFdD]?)";
		// Generex generex = new Generex(doubleRegex);
		// for (int i = 0; i < 20; i++) {
		// 	double wtf = 0XD.AP+0_0D;
		// 	String doubleStr = generex.random(2);
		// 	String doubleParse;
		// 	try {
		// 		doubleParse = String.valueOf(Double.parseDouble(doubleStr));
		// 	} catch (NumberFormatException e) {
		// 		doubleParse = e.getMessage();
		// 	}
		// 	System.out.println(doubleStr + " ".repeat(Math.max(1, 12 - doubleStr.length())) + doubleParse);
		// }

		// for (Reaction reaction : Reactions.reactions) {
		// 	String regex = reaction.getRegex();
		// 	StringBuilder sB = new StringBuilder();
		// 	boolean changed = false;
		// 	final char[] chars = regex.toCharArray();
		// 	for (int i = 0; i < chars.length - 4; i++) {
		// 		if (chars[i] == '[' && chars[i + 3] == ']'
		// 			&& Character.toLowerCase(chars[i + 1]) == chars[i + 2]) {
		// 			sB.append(chars[i + 2]);
		// 			i += 3;
		// 			changed = true;
		// 		} else {
		// 			sB.append(chars[i]);
		// 			if (chars[i] == '\\')
		// 				sB.append(chars[i]);
		// 		}
		// 	}
		// 	if (changed) {
		// 		String s = regex + "\n\t" + sB;
		// 		System.out.println(s);
		// 	}
		// }

		// System.out.println(String.valueOf((char) Integer.parseInt("0061", 16)));

		// final String abc = Pattern.compile("\\\\u([\\da-fA-F]{4})")
		// 						  .matcher(Commands.censorGeneRegex("coon"))
		// 						  .replaceAll(m -> String.valueOf((char) Integer.parseInt(m.group(1), 16)));
		// System.out.println(abc.length() + " " + abc);
		// System.out.println(Commands.censoredWordsGeneRegex);
		// for (int i = 0; i < 20; i++) {
		// 	System.out.println(new Generex(abc).random());
		// }

		// hiddenText();

		// UserCategory[] userCategories = UserCategory.values();
		// for (UserCategory userCategory1 : userCategories) {
		// 	for (UserCategory userCategory2 : userCategories) {
		// 		System.out.println(userCategory1 + "\t|\t"
		// 				+ userCategory2+ ": " +
		// 				userCategory1.compareTo(userCategory2) + " " +
		// 				userCategory1.inRange(userCategory2));
		// 	}
		// }

		// spaces("harita").forEach(System.out::println);

//		UserCategory[] userCategories = UserCategory.values();
//		for (UserCategory userCategory1 : userCategories) {
//			for (UserCategory userCategory2 : userCategories) {
//				System.out.println(userCategory1 + "\t|\t"
//						+ userCategory2+ ": " +
//						userCategory1.compareTo(userCategory2) + " " +
//						userCategory1.inRange(userCategory2));
//			}
//		}

		// String[] str = { "", "", "" };
		// for (int i = 0; i < 100; i++)  {
		// 	str[0] += String.valueOf(i % 3 == 0 ? 1 : 0);
		// 	final int parity = Integer.toBinaryString(i)
		// 						  .replaceAll("0", "")
		// 						  .length() % 2;
		// 	str[1] += String.valueOf(i % 3 == 0 && (parity == 0) ? 1 : 0);
		// 	// str[1] += String.valueOf(i % 5 == 0 ? 1 : 0);
		// 	str[2] += parity;
		// }
		// Arrays.stream(str).forEach(System.out::println);
		// int[] z = {};
		// z.clone();
		// for (int num = 0; num <= 0b111111; num++) {
		// 	System.out.println(String.format("%06d", Integer.parseInt(Integer.toBinaryString((num)))) + " " + ((0x96696996 & (1 << num)) == 0 ? 0 : 1) + " " +
		// 					   (Integer.toBinaryString(( ((num + 16) >> 5) ^ ((num + 4) >> 3) ^ ((num + 1) >> 1) )&1))); // OR 12, 1 // & 0b1010
		// }
		// System.out.println(lowestIndexOf(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5));
		// System.out.println(lowestIndexOf(new int[] {5, 7, 9, 10, 1, 3, 2, 4, 6, 8}, 5));
		// System.out.println(lowestIndexOf(new int[] {-5, 0, 3, 3, 5, 9, 9, 9, 9, 15}, 5));
		// System.out.println(lowestIndexOf(new int[] {9, 15, 9, 5, 3, 9, 0, 3, -5, 9}, 5));

		// final Date angelitteBirthday = new Date(122, Calendar.JULY, 16);
		//
		// Date current = new Date();
		// int days = angelitteBirthday.getDate() - current.getDate();
		// int months = angelitteBirthday.getMonth() - current.getMonth();
		// if (days < 0) {
		// 	days += YearMonth.of(current.getYear(), current.getMonth()).lengthOfMonth();
		// 	months--;
		// }
		// System.out.println((months != 0 ? Util.properCase(NumberToText.numberToString(months))
		// 								  + " Month" + (months != 1 ? "s" : "") : "")
		// 						+ (days != 0 ? (months != 0 ? " And " : "")
		// 									   + Util.properCase(NumberToText.numberToString(days))
		// 									   + " Day" + (days != 1 ? "s" : "") : "") + " Until.");

		// Map<Character, String> homoglyphs = new HashMap<>();
		// try {
		// 	URL path = Thread.currentThread().getContextClassLoader().getResource("json/jsonresources.json");
		// 	Objects.requireNonNull(path);
		// 	JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(path.getFile()));
		// 	jo.forEach((k, v) -> {
		// 		System.out.println(v);
		// 	});
		//
		// } catch (Exception e) {
		// 	System.out.println("fail");
		// }

		// Double[] sortedSubset = Stream.generate(() -> -1.0).limit(6L).toArray(Double[]::new);
		// sortSubArray(new Double[] {0.0, 0.5, 1.0, 1.5, 2.5, 2.5, 2.5, 3.0, 5.0, 6.0, 6.0, 7.0},
		// 			 new Double[] {3.0, 1.5, 6.0, 2.5, 2.5, 1.0},
		// 			 sortedSubset);
		// System.out.println(Arrays.toString(sortedSubset));

		System.out.println(Double.doubleToLongBits(-0.0) == Double.doubleToLongBits(0.0));
	}

	static <T> void sortSubArray(T[] sorted, T[] subset, T[] sortedSubset) { // subset.length == sortedSubset.length
		Map<T, Integer> subsetFreqs = new HashMap<>();
		for (T t : subset)
			subsetFreqs.compute(t, (k, v) -> v == null ? 1 : v + 1);
		int ind = 0;
		for (T t : sorted)
			if (subsetFreqs.compute(t, (k, v) -> v == null ? -1 : v - 1) >= 0)
				sortedSubset[ind++] = t;
	}

	private static String removeDuplicates(String s) {
		return s.length() <= 1 ? s : s.charAt(0) == s.charAt(1) ? removeDuplicates(s.substring(1)) : s.charAt(0) + removeDuplicates(s.substring(1));
	}

	private static int lowestIndexOf(int[] arr, int k) {
		return (int) Arrays.stream(arr).filter(i -> i < k).count();
	}

	private static int not(int i) {
		return i == 0 ? 1 : 0;
	}

	static class Position {
		final int x;
		final int y;
		final int dir;

		public Position(final int x, final int y, final int dir) {
			this.x = x;
			this.y = y;
			this.dir = dir;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			final Position position = (Position) o;
			return x == position.x && y == position.y && dir == position.dir;
		}
	}

	public static boolean isRobotBounded(String s) {
		int x=0,y=0,d=0;
			for(int i:s.toCharArray()){
				switch(i){
					case 71:
						switch(d){
							case 0:
								y++;
								break;
							case 1:
								x--;
								break;
							case 2:
								y--;
								break;
							case 3:
								x++;
								break;
						}
						break;
					case 76:
						d = (d + 1) % 4;
						break;
					case 82:
						d = (d - 1) % 4;
						if (d == -1)
							d = 3;
						break;
				}
			}
			System.out.printf("(%d, %d) %d\n", x, y, d);
		return d!=0||x==0&&y==0;
	}

	public static <T> void explanationOfOrChainControlFlow(Supplier<T>[] optionalGetters) {
		T nullableT = optionalGetters[0].get();
		if (nullableT != null) {
			return;
		} else {
			nullableT = optionalGetters[1].get();
			if (nullableT != null) {
				return;
			} else {
				nullableT = optionalGetters[2].get();
				if (nullableT != null) {
					return;
				} else {
					/* ... */
				}

			}
		}
	}

	public static <T> void explanationOfOrChain(Stream<Optional<T>> optionalGetters) {
		optionalGetters.reduce((a, b) -> a.or(() -> b)).get();
	}

	public static String fixCaseSensitive(String regex) {
		StringBuilder sB = new StringBuilder();
		final char[] chars = regex.toCharArray();
		for (int i = 0; i < chars.length - 4; i++) {
			if (chars[i] == '[' && chars[i + 3] == ']'
				&& Character.toLowerCase(chars[i + 1]) == chars[i + 2]) {
				sB.append(chars[i + 2]);
				i += 3;
			} else {
				sB.append(chars[i]);
				if (chars[i] == '\\')
					sB.append(chars[i]);
			}
		}
		return sB.toString();
	}

	public static List<String> spaces(String word) {
		List<String> spaces = new ArrayList<>();
		spaces.add(String.valueOf(word.charAt(0)));
		for (char c : word.substring(1).toCharArray()) {
			List<String> nextSpace = new ArrayList<>();
			spaces.forEach(w -> nextSpace.add(w + c));
			spaces.forEach(w -> nextSpace.add(w + " " + c));
			spaces = nextSpace;
		}
		return spaces;
	}

}

