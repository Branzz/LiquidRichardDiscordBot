package com.wordpress.brancodes.messaging.reactions.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

enum Unit { // TODO could be condensed to 1 method (but would we want more units later on?)
	KG(n -> applyScale(n, 2.2046) + " lbs"),
	LBS(n -> applyScale(n, 0.4536) + " kgs"),
	M(n -> withInches(n.multiply(new BigDecimal("3.2808")))),
	CM(n -> withInches(n.multiply(new BigDecimal(".032808")))),
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
			default: return SAME;
		}
	}

	public String convert(String n) {
		return converter.apply(new BigDecimal(n));
	}

	private static String applyScale(BigDecimal input, double conversionFactor) {
		return input.multiply(new BigDecimal(conversionFactor)).setScale(input.intValue() == 0 ? 2 : input.intValue() < 10 ? 1 : 0, RoundingMode.HALF_UP).toString();
	}

	private static String withInches(BigDecimal feet) {
		final BigDecimal inches = feet.remainder(BigDecimal.ONE)
									  .multiply(new BigDecimal(12))
									  .setScale(0, RoundingMode.DOWN);
		feet = feet.setScale(0, RoundingMode.DOWN);
		return feet + "'" + inches + "\"";
	}

}
