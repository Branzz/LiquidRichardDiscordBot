package com.wordpress.brancodes.messaging.reactions.unit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.wordpress.brancodes.messaging.reactions.unit.BaseUnitType.LENGTH;
import static com.wordpress.brancodes.messaging.reactions.unit.BaseUnitType.MASS;
import static com.wordpress.brancodes.messaging.reactions.unit.Unit.*;

public class BMI { // unstable non-synchronously

	private static final Logger LOGGER = LoggerFactory.getLogger(BMI.class);

	public static BMI getBMI(Iterable<UnitMatch> matches) {
		return new BMI(matches);
	}

	final Iterable<UnitMatch> matches;

	int heightCount = 0, weightCount = 0;

	ScaledDecimal convertedHeight = null; // in kg
	ScaledDecimal convertedWeight = null; // in m

	ScaledDecimal height = null;
	ScaledDecimal weight = null;
	boolean isWeight;

	final boolean couldCalculate;
	String convertedString;
	String logString;

	ScaledDecimal potential;
	ScaledDecimal potentialConverted;

	static final double weightFrom = 0, weightTo = 400, heightFrom = .5, heightTo = 3;

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
			BigDecimal scaledBmi = getConvertedBMI().createScaled();
			double bmiDouble = scaledBmi.doubleValue();
			String bmi = scaledBmi.toString();
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
		return potentialConverted.getFull().compareTo(BigDecimal.valueOf(from)) > 0 && potentialConverted.getFull().compareTo(BigDecimal.valueOf(to)) < 0;
	}

	/**
	 * kg / m^2
	 */
	@NotNull
	private ScaledDecimal getConvertedBMI() {
		// LOGGER.info(convertedWeight + " " + convertedHeight);
		final int scale = Math.max(convertedWeight.getFull().scale(), convertedHeight.getFull().scale());
		return new ScaledDecimal(convertedWeight.createScaled(scale).divide(convertedHeight.createScaled(scale).pow(2), RoundingMode.HALF_UP), 1, RoundingMode.HALF_UP);
	}

	@NotNull
	private static String getBMIString(double bmiDouble, String bmi) {
		return bmi + " BMI (" + (bmiDouble < 12 ? "Ideal" : bmiDouble < 15 ? "Kinda Skinny" :
					bmiDouble < 18 ? "Normal" : bmiDouble < 22 ? "Kinda Fat" : bmiDouble < 26 ? "Fat" : "Extremely Fat") + ')';
		//			return bmi + " BMI (" + (bmiDouble < 18.5 ? "Underweight" : bmiDouble < 25 ? "Healthy" : bmiDouble < 30 ? "Overweight" : "Obese") + ')';
	}

	@NotNull
	private String getLogString(String bmi) {
		return "(" + convertedHeight.createScaled(2) + " m, " + convertedWeight.createScaled(1) + " kg) -> " + bmi + " BMI";
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
