package com.wordpress.brancodes.messaging.reactions.unit;

import com.wordpress.brancodes.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;

import static com.wordpress.brancodes.messaging.reactions.unit.BaseUnitType.*;

public enum Unit {
	KG(MASS, n -> convert(n, 2.2046226218487) + " lbs", n -> n, n -> n, 'k'),
	LB(MASS, n -> convert(n, 0.45359237) + " kgs", n -> convert(n, 0.45359237), n -> convert(n, 1 / 0.45359237), 'l', 'p'), // not actually mass, gravitational force on earth
	M(LENGTH, n -> convertedWithInches(n, 3.2808398950131), n -> n, n -> n, 'm'),
	CM(LENGTH, n -> convertedWithInches(n, .0328083989501), n -> convert(n, .01), n -> convert(n, 100), 'c'),
	FT(LENGTH, n -> convert(n, 0.3048) + " m", n -> convert(n, 0.3048), n -> convert(n, 1 / 0.3048), 'f'),
	IN(LENGTH, n -> convert(n, 2.54) + " cm", n -> convert(n, .0254), n -> convert(n, 1 / .0254), 'i');

	private static final Logger LOGGER = LoggerFactory.getLogger(Unit.class);

	final BaseUnitType baseUnitType;
	final Function<ScaledDecimal, String> converter;
	final Function<ScaledDecimal, ScaledDecimal> normalizer; // to baseUnitType.normal
	final Function<ScaledDecimal, ScaledDecimal> fromNormal; // from baseUnitType.normal
	private char[] shortestSymbol;

	Unit(BaseUnitType baseUnitType, Function<ScaledDecimal, String> converter, Function<ScaledDecimal, ScaledDecimal> normalizer,
		 Function<ScaledDecimal, ScaledDecimal> fromNormal, char... shortestSymbol) {
		this.baseUnitType = baseUnitType;
		this.converter = converter;
		this.normalizer = normalizer;
		this.fromNormal = fromNormal;
		this.shortestSymbol = shortestSymbol;
	}

	// private static Map<Character, Unit> shortestSymbolMap = Arrays.stream(values())
	// 															  .map(u -> Pair.of(u.shortestSymbol, u)).

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

	public ScaledDecimal convertToUnit(ScaledDecimal n, Unit targetUnit) {
		return targetUnit.fromNormal.apply(normalizer.apply(n));
	}

	public String convert(ScaledDecimal n) {
		return converter.apply(n);
	}

	public String convert(String n) {
		return convert(new ScaledDecimal(new BigDecimal(n)));
	}

	private static ScaledDecimal convert(ScaledDecimal input, double conversionFactor) {
		return new ScaledDecimal(input.getFull().multiply(new BigDecimal(conversionFactor)), Math.max(1, input.getFull().stripTrailingZeros().scale()));
	}

	private static String convertedWithInches(ScaledDecimal input, double scale) {
		BigDecimal converted = input.getFull().multiply(new BigDecimal(scale));
		ScaledDecimal inches = new ScaledDecimal(
				converted.remainder(BigDecimal.ONE).multiply(new BigDecimal(12)),
				Math.max(0, input.getFull().stripTrailingZeros().scale()),
				RoundingMode.HALF_UP);
		ScaledDecimal feet = new ScaledDecimal(converted, 0, RoundingMode.DOWN);
		if (feet.createScaled().compareTo(BigDecimal.ZERO) == 0)
			return inches + "\"";
		if (inches.createScaled().compareTo(BigDecimal.ZERO) == 0)
			return feet + "'";
		return feet + "'" + inches + "\"";
	}

	/**
	 * @return inches converted to feet combined with any feet
	 */
	public static ScaledDecimal inchesToFeet(String feet, String inches) {
		return new ScaledDecimal(BigDecimal.valueOf(Double.parseDouble(feet) + (Double.parseDouble(inches) / 12)));
	}

	private static <T> T log(T t) {
		LOGGER.info(t.toString());
		return t;
	}

}
