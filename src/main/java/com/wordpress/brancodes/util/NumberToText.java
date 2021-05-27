package com.wordpress.brancodes.util;
import java.math.BigDecimal;
import java.util.HashMap;

public final class NumberToText {

//	private static HashMap<Integer, String> numberWords;
	private static final String[] onesPlaceNumberNames = {
			"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };
	private static final String[] teensNumberNames = {
			"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };
	private static final String[] tensPlaceNumberNames = {
			"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" };
	private static final String[] placeNumberNames = {
			"hundred", "thousand", "million", "billion", "trillion", "quadrillion", "quintillion",
			"sextillion", "septillion", "octillion", "nonillion", "decillion"};
	private static final String[] onesPlaceOrdinalNumberNames = {
			"", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eigth", "nineth" };
	private static final String[] teensOrdinalNumberNames = {
			"tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth" };
	private static final String[] tensOrdinalOrdinalPlaceNumberNames = {
			"", "tenth", "twentieth", "thirtieth", "fourtieth", "fiftieth", "sixtieth", "seventieth", "eightieth", "ninetieth" };
	private static final String[] placeOrdinalNumberNames = {
			"hundredth", "thousandth", "millionth", "billionth", "trillionth", "quadrillionth", "quintillionth",
			"sextillionth", "septillionth", "octillionth", "nonillionth", "decillionth"};
	private static final String[] onesPlaceFractionalNumberNames = {
			"whole", "half", "quarter"};

	public static String numberToString(int num) {
		if (num == 0)
			return "zero";
		StringBuilder str = new StringBuilder();
		if (num < 0) {
			str.append("negative ");
			num *= -1;
		}
		int length = (int) Math.log10(num);
		for (int i = length; i >= 0; i--) {
			int currentDigit = getDigit(i, num);
			if (i % 3 == 0) {
//				if (num >= 10)
//					str.append(" ");
				if (i != 0 || ((getDigit(i + 1, num) != 1)))
					str.append(onesPlaceNumberNames[currentDigit]);
				if (i != 0 && (!(i <= length - 2 && i > 2)
						|| !(currentDigit == 0 && getDigit(i + 1, num) == 0 && getDigit(i + 2, num) == 0))) {
					str.append(" ");
					str.append(placeNumberNames[i / 3]);
				}
			}
			else if (i % 3 == 1) {
				if (i <= length - 1) // && getDigit(i + 1, num) != 0
					str.append(" ");
				if (currentDigit == 1) {
					str.append(teensNumberNames[getDigit(i - 1, num)]);
//					if (num < 100)
//						i--;
//					str.append(" ");
				}
				else {
					str.append(tensPlaceNumberNames[currentDigit]);
					if (getDigit(i - 1, num) != 0 && currentDigit != 0)
						str.append("-");
				}
			}
			else if (i % 3 == 2) {
				if (i != length && currentDigit > 0)
					str.append(" ");
				str.append(onesPlaceNumberNames[currentDigit]);
				if (currentDigit != 0) {
					str.append(" ");
					str.append(placeNumberNames[0]); // hundred
				}
			}
		}
		return str.toString();
	}

	/**
	 * a b c. d
	 * 2 1 0.-1
	 */
	public static int getDigit(int digit, int num) {
		return (int) (num / Math.pow(10, digit) % 10);
	}

	public static String numberToStringDecimal(double num) {
		StringBuilder str = new StringBuilder();
		str.append(numberToString((int) num));
		double decimal = num % 1;
//		if (decimal != 0) {
		str.append("point ");
//		}
		return str.toString();
	}

	public static String numberToString(double num) {
		if (num == 0)
			return "zero";
		StringBuilder str = new StringBuilder();
		if ((int) num != 0)
			str.append(numberToString((int) num));
		double decimal = num % 1;
		if (decimal != 0) {
			if ((int) num != 0)
				str.append(" and ");

			String s = Double.toString(num);
			int length = s.split("\\.")[1].length(); // TODO source
			str.append(numberToString((int) (decimal * Math.pow(10, length))));

			str.append(" ");
			if (length % 3 == 0) {
//				str.append(onesPlaceNumberNames[1]); // one
			}
			else if (length % 3 == 1) {
				str.append(tensPlaceNumberNames[1]); // ten
			}
			else if (length % 3 == 2) {
				str.append(placeNumberNames[0]); // hundred
			}
			
			if (length > 2) {
				if (length % 3 != 0)
					str.append("-");
				str.append(placeNumberNames[length / 3]); // placeFractionalNumberNames
			}
			str.append(decimal == .1 ? "th" : "ths");
		}
		return str.toString();
	}

}
