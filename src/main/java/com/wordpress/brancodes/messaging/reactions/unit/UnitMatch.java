package com.wordpress.brancodes.messaging.reactions.unit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.regex.MatchResult;
// keep copy of original MatchResult ? (potential memory leak)
// (or does that allow users to ruin the purpose of this class)
public class UnitMatch {

	final String fullMatch;
	final String negatives;
	final String feetInchBase;
	final boolean isFeetInch; // as opposed to other units
	String feet;
	String inches;
	String inchDecimal;
	boolean hasInchWhole;
	ScaledDecimal value;
	Unit unit;

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
			value = new ScaledDecimal(new BigDecimal(match.group(12)));
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
	public ScaledDecimal toFeetInchUnit() {
		return getInchFeetSum().setScale(getScale(), RoundingMode.HALF_UP);
	}

	public String toFeetInchString() {
		int scale = getScale();
		final ScaledDecimal product = new ScaledDecimal(getInchFeetSum().getFull().multiply(new BigDecimal("0.3048")));
		final boolean cmRange = product.getFull().compareTo(new BigDecimal("2")) <= 0;
		product.setScale(cmRange ? scale + 1 : scale, RoundingMode.HALF_UP);
		if (cmRange)
			return product.createScaled().movePointRight(2) + " cm";
		else
			return product + " m";
	}

	private int getScale() {
		if (!hasInchWhole) {
			return Math.max(0, 2 + new BigDecimal(feet).stripTrailingZeros().scale());
		} else {
			return Math.max(2, inchDecimal.length());
		}
	}

	private ScaledDecimal getInchFeetSum() {
		return new ScaledDecimal(BigDecimal.valueOf(Double.parseDouble(feet) + (Double.parseDouble(inches) / 12)));
	}

}