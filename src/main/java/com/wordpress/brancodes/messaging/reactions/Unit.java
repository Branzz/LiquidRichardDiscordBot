package com.wordpress.brancodes.messaging.reactions;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.MatchResult;

enum Unit { // TODO could be condensed to 1 method (but would we want more units later on?)
	KG(n -> applyScale(n, 2.2046226218487) + " lbs", n -> n, n -> n),
	LBS(n -> applyScale(n, 0.45359237) + " kgs", n -> applyScale(n, 0.45359237), n -> applyScale(n, 1 / 0.45359237)),
	M(n -> scaleWithInches(n, 3.2808398950131), n -> n, n -> n),
	CM(n -> scaleWithInches(n, .0328083989501), n -> applyScale(n, 100), n -> applyScale(n, .01)),
	FT(n -> applyScale(n, 0.3048) + " m", n -> applyScale(n, 0.3048), n -> applyScale(n, 1 / 0.3048)),
	IN(n -> applyScale(n, 2.54) + " cm", n -> applyScale(n, .0254), n -> applyScale(n, 1 / .0254));

	private static final Logger LOGGER = LoggerFactory.getLogger(Unit.class);

	private final Function<BigDecimal, String> converter;
	private final Function<BigDecimal, BigDecimal> normalizer;
	private final Function<BigDecimal, BigDecimal> fromNormal;

	Unit(Function<BigDecimal, String> converter, Function<BigDecimal, BigDecimal> normalizer, Function<BigDecimal, BigDecimal> fromNormal) {
		this.converter = converter;
		this.normalizer = normalizer;
		this.fromNormal = fromNormal;
	}

	public static Unit of(final String name) {
		switch (name.charAt(0)) {
			case 'K': case 'k': return KG;
			case 'L': case 'l': case 'P': case 'p': return LBS;
			case 'M': case 'm': return M;
			case 'C': case 'c': return CM;
			case 'F': case 'f': return FT;
			case 'I': case 'i': return IN;
			default: return null;
		}
	}

	public BigDecimal convertToUnit(BigDecimal n, Unit targetUnit) {
		return targetUnit.fromNormal.apply(normalizer.apply(n));
	}

	public String convert(BigDecimal n) {
		return converter.apply(n);
	}

	public String convert(String n) {
		return convert(new BigDecimal(n));
	}

	private static BigDecimal applyScale(BigDecimal input, double conversionFactor) {
		return input.multiply(new BigDecimal(conversionFactor)).setScale(Math.max(0, input.stripTrailingZeros().scale()), RoundingMode.HALF_UP);
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
			if (inchDecimal == null)
				inchDecimal = "0";
			converted = convertFeetInchString(match.group(5), match.group(6), inchWhole, inchDecimal);
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

	static BigDecimal convertFeetInchUnit(MatchResult match) {
		if (match.group(3) != null) {
			String inchWhole = match.group(7);
			String inchDecimal;
			if (inchWhole == null) {
				inchWhole = "0";
				inchDecimal = match.group(10);
			} else {
				inchDecimal = match.group(9);
			}
			if (inchDecimal == null)
				inchDecimal = "0";
			return convertFeetInchUnit(match.group(5), match.group(6), inchWhole, inchDecimal);
		} else
			return null;
	}
	/**
	 * @return to feet + inch
	 */
	public static BigDecimal convertFeetInchUnit(String feet, final String inches, String inchWhole, String inchDecimal) {
		int scale = getScale(inchWhole, feet, inchDecimal);
		final BigDecimal product = Unit.inchesToFeet(feet, inches == null ? "0" : inches);
		return product.setScale(scale, RoundingMode.HALF_UP);
	}

	public static String convertFeetInchString(String feet, final String inches, String inchWhole, String inchDecimal) {
		int scale = getScale(inchWhole, feet, inchDecimal);
		final BigDecimal product = Unit.inchesToFeet(feet, inches == null ? "0" : inches).multiply(new BigDecimal("0.3048"));
		final boolean cmRange = product.compareTo(new BigDecimal("1")) <= 0;
		final BigDecimal converted = product.setScale(cmRange ? scale + 1 : scale, RoundingMode.HALF_UP);
		if (cmRange)
			return converted.movePointRight(2) + " cm";
		else
			return converted + " m";
	}

	private static int getScale(String inchWhole, String feet, String inchDecimal) {
		int scale;
		if (inchWhole == null) {
			scale = Math.max(0, 2 + new BigDecimal(feet).stripTrailingZeros().scale());
		} else {
			scale = inchDecimal == null ? 2 : Math.max(2, inchDecimal.length());
		}
		return scale;
	}

	public static Entry<String, String> getBMI(Iterable<MatchResult> matches) { // TODO convert (BMI/this method) to class?
		BigDecimal convertedHeight = null, convertedWeight = null;
		BigDecimal height = null, weight = null;
		int heightCount = 0, weightCount = 0;
		for (MatchResult match : matches) {
			try {
				if (match.group(3) != null) {
					BigDecimal potentialHeight = Unit.convertFeetInchUnit(match);
					BigDecimal potentialConvertedHeight = Unit.FT.convertToUnit(potentialHeight, Unit.M);
					if (potentialConvertedHeight.compareTo(BigDecimal.valueOf(.5)) > 0
							&& potentialConvertedHeight.compareTo(BigDecimal.valueOf(3)) < 0) {
						height = potentialHeight;
						convertedHeight = potentialConvertedHeight;
						heightCount++;
					}
				} else {
					Unit unit = Unit.of(match.group(13));
					if (unit == Unit.CM || unit == Unit.M || unit == Unit.FT || unit == Unit.IN) {
						BigDecimal potentialHeight = new BigDecimal(match.group(12));
						BigDecimal potentialConvertedHeight = Unit.of(match.group(13)).convertToUnit(potentialHeight, Unit.M);
						if (potentialConvertedHeight.compareTo(BigDecimal.valueOf(.5)) > 0
								&& potentialConvertedHeight.compareTo(BigDecimal.valueOf(3)) < 0) {
							height = potentialHeight;
							convertedHeight = potentialConvertedHeight;
							heightCount++;
						}
					} else if (unit == Unit.KG || unit == Unit.LBS) {
						BigDecimal potentialWeight = new BigDecimal(match.group(12));
						BigDecimal potentialConvertedWeight = Unit.of(match.group(13)).convertToUnit(potentialWeight, Unit.KG);
						if (potentialConvertedWeight.compareTo(BigDecimal.valueOf(20)) > 0
								&& potentialConvertedWeight.compareTo(BigDecimal.valueOf(800)) < 0) {
							weight = potentialWeight;
							convertedWeight = potentialConvertedWeight;
							weightCount++;
						}
					}
				}
			} catch (NullPointerException npe) { // ignore the match if parsing/conversion fails
				LOGGER.error("failed convert " + match.group(0));
			}
		}
		if (heightCount == 1 && weightCount == 1) {
			BigDecimal bmiVal = getBMI(convertedHeight, convertedWeight);
			double bmiDouble = bmiVal.doubleValue();
			String bmi = bmiVal.toPlainString();
			return new SimpleEntry<>(bmi + " BMI (" + (bmiDouble < 18.5 ? "Underweight"
					: bmiDouble < 25 ? "Healthy" : bmiDouble < 30 ? "Overweight" : "Obese") + ')',
					"(" + height + " m, " + weight + " kg) -> " + bmi + " BMI");
		} else
			return null;
	}

	/**
	 * kg / m^2
	 * @param convertedHeight in kg
	 * @param convertedWeight in m
	 */
	@NotNull
	private static BigDecimal getBMI(BigDecimal convertedHeight, BigDecimal convertedWeight) {
		return convertedWeight.setScale(2, RoundingMode.HALF_UP)
				.divide(convertedHeight.setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP)
				.divide(convertedHeight, RoundingMode.HALF_UP)
				.setScale(1, RoundingMode.HALF_UP);
	}

}
