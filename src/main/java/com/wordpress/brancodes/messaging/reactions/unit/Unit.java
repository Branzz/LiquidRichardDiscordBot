package com.wordpress.brancodes.messaging.reactions.unit;

import com.wordpress.brancodes.messaging.reactions.Reactions;
import com.wordpress.brancodes.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.Reactions.apostrophes;
import static com.wordpress.brancodes.messaging.reactions.Reactions.doubleQuotes;
import static com.wordpress.brancodes.messaging.reactions.unit.BaseUnitType.*;
import static com.wordpress.brancodes.messaging.reactions.unit.UnitSystem.*;
import static com.wordpress.brancodes.util.JavaUtil.deepArrayMerge;
import static java.util.stream.IntStream.concat;

public enum Unit {
	KG(MASS, METRIC, n -> convert(n, 2.2046226218487) + " lbs", n -> n, n -> n, 'k'),
	LB(MASS, IMPERIAL, n -> convert(n, 0.45359237) + " kgs", n -> convert(n, 0.45359237), n -> convert(n, 1 / 0.45359237), 'l', 'p'), // not actually mass, gravitational force on earth
	M(LENGTH, METRIC, n -> convertedWithInches(n, 3.2808398950131), n -> n, n -> n, 'm'),
	CM(LENGTH, METRIC, n -> convertedWithInches(n, .0328083989501), n -> convert(n, .01), n -> convert(n, 100), 'c'),
	FT(LENGTH, IMPERIAL, n -> convert(n, 0.3048) + " m", n -> convert(n, 0.3048), n -> convert(n, 1 / 0.3048), 'f'),
	IN(LENGTH, IMPERIAL, n -> convert(n, 2.54) + " cm", n -> convert(n, .0254), n -> convert(n, 1 / .0254), (apostrophes + doubleQuotes + "i").toCharArray()),
	LAZY(null, null, null, null, null)
	;

	private static final Logger LOGGER = LoggerFactory.getLogger(Unit.class);

	final BaseUnitType baseUnitType;
	final UnitSystem unitSystem;
	final Function<ScaledDecimal, String> converter;
	final Function<ScaledDecimal, ScaledDecimal> normalizer; // to baseUnitType.normal
	final Function<ScaledDecimal, ScaledDecimal> fromNormal; // from baseUnitType.normal
	final char[] shortestSymbol;

	Unit(BaseUnitType baseUnitType, UnitSystem unitSystem, Function<ScaledDecimal, String> converter,
		 Function<ScaledDecimal, ScaledDecimal> normalizer, Function<ScaledDecimal, ScaledDecimal> fromNormal, char... shortestSymbol) {
		this.baseUnitType = baseUnitType;
		this.unitSystem = unitSystem;
		this.converter = converter;
		this.normalizer = normalizer;
		this.fromNormal = fromNormal;
		this.shortestSymbol = shortestSymbol;
	}

	// private static Map<Character, Unit> shortestSymbolMap = Arrays.stream(values())
	// 															  .map(u -> Pair.of(u.shortestSymbol, u)).

	private static final Map<Character, Unit> lookUpTable =
			Arrays.stream(values())
				  .flatMap(u -> new String(u.shortestSymbol)
									  .chars()
									  .flatMap(c -> IntStream.of(Character.toLowerCase(c), Character.toUpperCase(c)).distinct())
									  .mapToObj(c -> Pair.of((char) c, u)))
				  .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

	public static Unit of(String name) {
		return lookUpTable.get(name.charAt(0));
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
		return new ScaledDecimal(input.getFull().multiply(new BigDecimal(conversionFactor)), Math.max(1, input.getScale()));
	}

	private static String convertedWithInches(ScaledDecimal input, double scale) {
		BigDecimal converted = input.getFull().multiply(new BigDecimal(scale));
		ScaledDecimal inches = new ScaledDecimal(
				converted.remainder(BigDecimal.ONE).multiply(new BigDecimal(12)),
				Math.max(0, input.getFull().stripTrailingZeros().scale()),
				RoundingMode.HALF_UP);
		ScaledDecimal feet = new ScaledDecimal(converted, 0, RoundingMode.DOWN);
		if (inches.toString().equals("12")) {
			feet = feet.copy().setBigDecimal(b -> b.add(BigDecimal.ONE));
			inches = inches.copy().setBigDecimal(ignored -> BigDecimal.ZERO);
		}
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

	public static Map<BaseUnitType, double[][]> conversionFactorMatrix() {
		return null; // TODO
	}

}
