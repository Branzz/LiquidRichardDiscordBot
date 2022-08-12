package com.wordpress.brancodes.messaging.reactions.unit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public class ScaledDecimal {

	private int scale;
	private BigDecimal bigDecimal;
	private RoundingMode roundingMode;
	private BigDecimal scaled;

	public ScaledDecimal(BigDecimal bigDecimal, int scale, RoundingMode roundingMode) {
		this.bigDecimal = bigDecimal;
		this.scale = scale;
		this.roundingMode = roundingMode;
		scaled = null;
	}

	public ScaledDecimal(BigDecimal bigDecimal, int scale) {
		this(bigDecimal, scale, RoundingMode.HALF_UP);
	}

	public ScaledDecimal(BigDecimal bigDecimal) {
		this(bigDecimal, bigDecimal.scale());
	}

	public BigDecimal getFull() {
		return bigDecimal;
	}

	public BigDecimal createScaled() {
		if (scaled != null)
			return scaled;
		return scaled = new BigDecimal(bigDecimal.toString()).setScale(scale, roundingMode);
	}

	public BigDecimal createScaled(int scale, RoundingMode roundingMode) {
		return scaled = new BigDecimal(bigDecimal.toString()).setScale(scale, roundingMode);
	}

	public BigDecimal createScaled(int scale) {
		return createScaled(scale, roundingMode);
	}

	public ScaledDecimal setScale(int scale, RoundingMode roundingMode) {
		this.scale = scale;
		this.roundingMode = roundingMode;
		resetScaled();
		return this;
	}

	public ScaledDecimal setScale(int scale) {
		this.scale = scale;
		resetScaled();
		return this;
	}

	public ScaledDecimal setBigDecimal(Function<BigDecimal, BigDecimal> setter) {
		bigDecimal = setter.apply(bigDecimal);
		resetScaled();
		return this;
	}

	private void resetScaled() {
		scaled = null;
	}

	public ScaledDecimal copy() {
		return new ScaledDecimal(new BigDecimal(bigDecimal.toString()), scale, roundingMode);
	}

	@Override
	public String toString() {
		return createScaled().toString();
	}

}
