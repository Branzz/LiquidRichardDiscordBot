package com.wordpress.brancodes.messaging.reactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.regex.MatchResult;

enum Unit { // TODO could be condensed to 1 method (but would we want more units later on?)
	KG(n -> applyScale(n, 2.2046226218487) + " lbs"),
	LBS(n -> applyScale(n, 0.45359237) + " kgs"),
	M(n -> scaleWithInches(n, 3.2808398950131)),
	CM(n -> scaleWithInches(n, .0328083989501)),
	FT(n -> applyScale(n, 0.3048) + " m"),
	IN(n -> applyScale(n, 2.54) + " cm"),
	SAME(BigDecimal::toString);

	private final Function<BigDecimal, String> converter;

	Unit(Function<BigDecimal, String> converter) {
		this.converter = converter;
	}

	public static Unit of(final String name) {
		switch (name.charAt(0)) {
			case 'K': case 'k': return KG;
			case 'L': case 'l': case 'P': case 'p': return LBS;
			case 'M': case 'm': return M;
			case 'C': case 'c': return CM;
			case 'F': case 'f': return FT;
			case 'I': case 'i': return IN;
			default: return SAME;
		}
	}


	public String convert(BigDecimal n) {
		return converter.apply(n);
	}

	public String convert(String n) {
		return convert(new BigDecimal(n));
	}

	private static String applyScale(BigDecimal input, double conversionFactor) {
		return input.multiply(new BigDecimal(conversionFactor)).setScale(Math.max(0, input.stripTrailingZeros().scale()), RoundingMode.HALF_UP).toString();
	}

	private static String scaleWithInches(BigDecimal input, double scale) {
		BigDecimal scaled = input.multiply(new BigDecimal(scale));
		BigDecimal inches = scaled.remainder(BigDecimal.ONE)
								.multiply(new BigDecimal(12))
								.setScale(Math.max(0, input.stripTrailingZeros().scale()), RoundingMode.HALF_UP);
		BigDecimal feet = scaled.setScale(0, RoundingMode.DOWN);
		if (feet.equals(BigDecimal.ZERO))
			return inches + "\"";
		if (inches.equals(BigDecimal.ZERO))
			return feet + "'";
		return feet + "'" + inches + "\"";
	}

	/**
	 * @return inches converted to feet combined with any feet
	 */
	public static BigDecimal inchesToFeet(String feet, String inches) {
		return BigDecimal.valueOf(Double.parseDouble(feet) + (Double.parseDouble(inches) / 12));
	}

	/**
	 * @return null if not convertible
	 */
	public static String convertUnit(final MatchResult match) {
		// Matcher x = null;
		// x.group("inches");
		String converted;
		// for (int i = 4; i <= 15; i++) {
		// 	System.out.println(i + ": " + match.group(i));
		// }
		if (match.group(3) != null) {
			if (match.group(4).equals("5'11"))
				return "Short.";
			String inchWhole = match.group(7);
			String inchDecimal;
			if (inchWhole == null) {
				inchWhole = "0";
				inchDecimal = match.group(10);
			} else {
				inchDecimal = match.group(9);
			}
			converted = convertFeetInchUnit(match.group(5), match.group(6), inchWhole, inchDecimal);
		} else
			converted = Unit.of(match.group(13)).convert(match.group(12));
		if (match.group(1).length() % 2 == 1)
			return '-' + converted;
		else
			return converted;
		// for (int i = 0; i < match.groupCount(); i++) {
		// 	System.out.println(i + ":" + match.group(i));
		// }
		// return null;
	}

	/**
	 * @return null if not convertible
	 */
	public static String convertFeetInchUnit(String feet, final String inches, String inchWhole, String inchDecimal) {
		int scale;
		if (inchWhole == null) {
			scale = Math.max(0, 2 + new BigDecimal(feet).stripTrailingZeros().scale());
		} else {
			scale = inchDecimal == null ? 2 : Math.max(2, inchDecimal.length());
		}
		final BigDecimal product = Unit.inchesToFeet(feet, inches).multiply(new BigDecimal("0.3048"));
		final boolean cmRange = product.compareTo(new BigDecimal("10")) < 0;
		final BigDecimal converted = product.setScale(cmRange ? scale + 1 : scale, RoundingMode.HALF_UP);
		if (cmRange)
			return converted.movePointRight(2) + " cm";
		else
			return converted + " m";
	}

}
