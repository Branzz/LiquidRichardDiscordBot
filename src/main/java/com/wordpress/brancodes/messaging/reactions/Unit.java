package com.wordpress.brancodes.messaging.reactions;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.regex.MatchResult;

import static com.wordpress.brancodes.messaging.reactions.Unit.BaseUnitType.LENGTH;
import static com.wordpress.brancodes.messaging.reactions.Unit.BaseUnitType.MASS;

enum Unit {
	KG(MASS, n -> applyScale(n, 2.2046226218487) + " lbs", n -> n, n -> n),
	LB(MASS, n -> applyScale(n, 0.45359237) + " kgs", n -> applyScale(n, 0.45359237), n -> applyScale(n, 1 / 0.45359237)), // not actually mass, gravitational force on earth
	M(LENGTH, n -> scaleWithInches(n, 3.2808398950131), n -> n, n -> n),
	CM(LENGTH, n -> scaleWithInches(n, .0328083989501), n -> applyScale(n, .01), n -> applyScale(n, 100)),
	FT(LENGTH, n -> applyScale(n, 0.3048) + " m", n -> applyScale(n, 0.3048), n -> applyScale(n, 1 / 0.3048)),
	IN(LENGTH, n -> applyScale(n, 2.54) + " cm", n -> applyScale(n, .0254), n -> applyScale(n, 1 / .0254));

	enum BaseUnitType {
		MASS(KG), LENGTH(M); //, TIME, TEMPERATURE, AMOUNT, CURRENT, LUMINOSITY;

		private final Unit normal;

		BaseUnitType(Unit normal) {
			this.normal = normal;
		}

		public Unit getNormalUnit() {
			return normal;
		}

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Unit.class);

	private final BaseUnitType baseUnitType;
	private final Function<BigDecimal, String> converter;
	private final Function<BigDecimal, BigDecimal> normalizer; // to baseUnitType.normal
	private final Function<BigDecimal, BigDecimal> fromNormal; // from baseUnitType.normal

	Unit(BaseUnitType baseUnitType, Function<BigDecimal, String> converter, Function<BigDecimal, BigDecimal> normalizer, Function<BigDecimal, BigDecimal> fromNormal) {
		this.baseUnitType = baseUnitType;
		this.converter = converter;
		this.normalizer = normalizer;
		this.fromNormal = fromNormal;
	}

	public static Unit of(String name) {
		switch (name.charAt(0)) {
			case 'K': case 'k': return KG;
			case 'L': case 'l': case 'P': case 'p': return LB;
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
		return input.multiply(new BigDecimal(conversionFactor)).setScale(Math.max(1, input.stripTrailingZeros().scale()), RoundingMode.HALF_UP);
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

	// keep copy of original MatchResult ? (potential memory leak)
	// (or does that allow users to ruin the purpose of this class)
	public static class UnitMatch {

		private final String fullMatch;
		private final String negatives;
		private final String feetInchBase;
		private final boolean isFeetInch; // as opposed to other units
		private String feet;
		private String inches;
		private String inchDecimal;
		private boolean hasInchWhole;
		private BigDecimal value;
		private Unit unit;

		public UnitMatch(MatchResult match) {
			fullMatch = match.group(0);
			negatives = match.group(1);
			isFeetInch = match.group(3) != null;
			feetInchBase = match.group(4);
			if (isFeetInch) {
				feet = match.group(5);
				if (feet == null)
					feet = "0";
				inches = match.group(6);
				if (inches == null)
					inches = "0";
				hasInchWhole = match.group(7) != null;
				if (!hasInchWhole) {
					inchDecimal = match.group(9);
				} else {
					inchDecimal = match.group(10);
				}
				if (inchDecimal == null)
					inchDecimal = "0";
			} else {
//			value = Optional.ofNullable(match.group(12)).map(BigDecimal::new).orElse(null);
//			value = nullify(match.group(12), BigDecimal::new);
				value = new BigDecimal(match.group(12));
				unit = Unit.of(match.group(13));
			}

		}

		public static <T, R> R nullify(T val, Function<T, R> process) {
			return val == null ? null : process.apply(val);
		}

		public String fullMatch() {
			return fullMatch;
		}

		/**
		 * @return null if not convertible
		 */
		public String convertUnit() {
			String converted;
			if (isFeetInch) {
				if (feetInchBase.equals("5'11"))
					return "Short.";
				else
					converted = toFeetInchString();
			} else
				converted = unit.convert(value);
			if (negatives.length() % 2 == 1)
				return '-' + converted;
			else
				return converted;
			// for (int i = 0; i < match.groupCount(); i++) {
			// 	System.out.println(i + ":" + match.group(i));
			// }
			// return null;
		}

		/**
		 * @return to feet + inch
		 */
		public BigDecimal toFeetInchUnit() {
			return getInchFeetSum().setScale(getScale(), RoundingMode.HALF_UP);
		}

		public String toFeetInchString() {
			int scale = getScale();
			final BigDecimal product = getInchFeetSum().multiply(new BigDecimal("0.3048"));
			final boolean cmRange = product.compareTo(new BigDecimal("2")) <= 0;
			final BigDecimal converted = product.setScale(cmRange ? scale + 1 : scale, RoundingMode.HALF_UP);
			if (cmRange)
				return converted.movePointRight(2) + " cm";
			else
				return converted + " m";
		}

		private int getScale() {
			if (!hasInchWhole) {
				return Math.max(0, 2 + new BigDecimal(feet).stripTrailingZeros().scale());
			} else {
				return Math.max(2, inchDecimal.length());
			}
		}

		private BigDecimal getInchFeetSum() {
			return BigDecimal.valueOf(Double.parseDouble(feet) + (Double.parseDouble(inches) / 12));
		}

	}

	public static BMI getBMI(Iterable<UnitMatch> matches) {
		return new BMI(matches);
	}

	public static class BMI { // unstable non-synchronously
		private final Iterable<UnitMatch> matches;

		private int heightCount = 0, weightCount = 0;

		private BigDecimal convertedHeight = null; // in kg
		private BigDecimal convertedWeight = null; // in m

		private BigDecimal height = null;
		private BigDecimal weight = null;
		private boolean isWeight;

		private final boolean couldCalculate;
		private String convertedString;
		private String logString;

		private BigDecimal potential;
		private BigDecimal potentialConverted;

		static final double weightFrom = 20, weightTo = 400, heightFrom = .5, heightTo = 3;

		public BMI(final Iterable<UnitMatch> matches) {
			this.matches = matches;
			couldCalculate = calculateBMI();
		}

		public synchronized boolean calculateBMI() {
			for (UnitMatch match : matches) {
				try {
					if (match.isFeetInch) {
						potential = match.toFeetInchUnit();
						potentialConverted = FT.convertToUnit(potential, M);
						isWeight = false;
						setToPotential();
					} else {
						if (match.unit.baseUnitType == LENGTH) {
							isWeight = false;
							setToPotential(match, M);
						}
						else if (match.unit.baseUnitType == MASS) {
							isWeight = true;
							setToPotential(match, KG);
						}
					}
				} catch (NullPointerException npe) { // ignore the match if parsing/conversion fails
					LOGGER.error("failed to convert " + match.fullMatch);
				}
			}
			if (heightCount == 1 && weightCount == 1) {
				BigDecimal bmiVal = getConvertedBMI();
				double bmiDouble = bmiVal.doubleValue();
				String bmi = bmiVal.toPlainString();
				convertedString = getBMIString(bmiDouble, bmi);
				logString = getLogString(bmi);
				return true;
			} else
				return false;
		}

		private synchronized void setToPotential(UnitMatch match, Unit toUnit) {
			potential = match.value;
			potentialConverted = match.unit.convertToUnit(potential, toUnit);
			setToPotential();
		}

		private synchronized void setToPotential() {
			if (inRange()) {
				if (isWeight) {
					setWeightToPotential();
				} else {
					setHeightToPotential();
				}
			}
		}

		private synchronized void setHeightToPotential() {
			height = potential;
			convertedHeight = potentialConverted;
			heightCount++;
		}

		private synchronized void setWeightToPotential() {
			weight = potential;
			convertedWeight = potentialConverted;
			weightCount++;
		}

		private boolean inRange() {
			return inRange(isWeight ? weightFrom : heightFrom, isWeight ? weightTo : heightTo);
		}

		private boolean inRange(double from, double to) {
			return potentialConverted.compareTo(BigDecimal.valueOf(from)) > 0 && potentialConverted.compareTo(BigDecimal.valueOf(to)) < 0;
		}

		/**
		 * kg / m^2
		 */
		@NotNull
		private BigDecimal getConvertedBMI() {
			return convertedWeight.setScale(3, RoundingMode.HALF_UP)
								  .divide(convertedHeight.setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP)
								  .divide(convertedHeight, RoundingMode.HALF_UP)
								  .setScale(1, RoundingMode.HALF_UP);
		}

		@NotNull
		private static String getBMIString(double bmiDouble, String bmi) {
			return bmi + " BMI (" + (bmiDouble < 12 ? "Ideal" : bmiDouble < 15 ? "Kinda Skinny" : bmiDouble < 18 ? "Normal" : bmiDouble < 22 ? "Kinda Fat" : "Fat") + ')';
//			return bmi + " BMI (" + (bmiDouble < 18.5 ? "Underweight" : bmiDouble < 25 ? "Healthy" : bmiDouble < 30 ? "Overweight" : "Obese") + ')';
		}

		@NotNull
		private String getLogString(String bmi) {
			return "(" + height + " m, " + weight + " kg) -> " + bmi + " BMI";
		}

		public boolean couldCalculate() {
			return couldCalculate;
		}

		public String getConvertedString() {
			return convertedString;
		}

		public String getLogString() {
			return logString;
		}

	}

}
