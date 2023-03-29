package com.wordpress.brancodes.messaging.reactions.unit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.wordpress.brancodes.messaging.reactions.unit.BaseUnitType.LENGTH;
import static com.wordpress.brancodes.messaging.reactions.unit.BaseUnitType.MASS;
import static com.wordpress.brancodes.messaging.reactions.unit.Unit.*;
import static com.wordpress.brancodes.messaging.reactions.unit.UnitSystem.IMPERIAL;

public class BMI { // unstable non-synchronously

	private static final Logger LOGGER = LoggerFactory.getLogger(BMI.class);

	final Iterable<UnitMatch> matches;
	final List<UnitMatch> actualMatches;

	int heightCount = 0, weightCount = 0;

	ScaledDecimal convertedHeight; // in kg
	ScaledDecimal convertedWeight; // in m

	ScaledDecimal height;
	ScaledDecimal weight;
	boolean isWeight;

	Unit unit; // potential
	Unit heightUnit; // known type
	Unit weightUnit;

	boolean couldCalculate;
	String convertedString;
	String logString;

	ScaledDecimal potential;
	ScaledDecimal potentialConverted;

	boolean lazy = false;

	static final double weightFrom = 0, lazyWeightFrom = 30, weightTo = 400, heightFrom = .5, heightTo = 3;

	public BMI(Iterable<UnitMatch> matches) {
		this.matches = matches;
		actualMatches = new ArrayList<>();
		calculateBMI();
	}

	public List<UnitMatch> getActualMatches() {
		return actualMatches;
	}

	public synchronized void calculateBMI() {
		List<UnitMatch> lazies = new ArrayList<>();
		for (UnitMatch match : matches) {
			if (match.unit == LAZY) {
				lazies.add(match);
			} else {
				actualMatches.add(match); // TODO remove
				try {
					if (match.isFeetInch) {
						potential = match.toFeetInchUnit();
						unit = FT;
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
				} catch (NullPointerException ignored) { // ignore the match if parsing/conversion fails
					LOGGER.error("failed to convert " + match.fullMatch);
				}
			}
		}
		int laziesInRange = 0;
		UnitMatch validLazy = null;
		lazy = true;
		if (heightCount == 1 && weightCount == 0) {
			isWeight = true;
			for (UnitMatch lazy : lazies) {
				lazy.unit = heightUnit.unitSystem == IMPERIAL ? LB : KG;
				setToPotential(lazy, KG);
				if (inRange()) {
					validLazy = lazy;
					laziesInRange++;
				}
			}
			// if (laziesInRange == 1) {
			//
			// }
		} else if (heightCount == 0 && weightCount == 1) {
			isWeight = false;
			for (UnitMatch lazy : lazies) {
				lazy.unit = weightUnit.unitSystem == IMPERIAL ? FT : CM;
				setToPotential(lazy, KG);
				if (inRange()) {
					validLazy = lazy;
					laziesInRange++;
				}
			}
		}
		if (laziesInRange == 1) {
			actualMatches.add(validLazy);
		}
		if (heightCount == 1 && weightCount == 1) {
			BigDecimal scaledBmi = getConvertedBMI().createScaled();
			double bmiDouble = scaledBmi.doubleValue();
			String bmi = scaledBmi.toString();
			convertedString = getBMIString(bmiDouble, bmi);
			logString = getLogString(bmi);
			couldCalculate = true;
		} else
			couldCalculate = false;
	}

	private synchronized void setToPotential(UnitMatch match, Unit toUnit) {
		potential = match.value;
		unit = match.unit;
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
		heightUnit = unit;
		convertedHeight = potentialConverted;
		heightCount++;
	}

	private synchronized void setWeightToPotential() {
		weight = potential;
		weightUnit = unit;
		convertedWeight = potentialConverted;
		weightCount++;
	}

	private boolean inRange() {
		return inRange(isWeight ? (lazy ? lazyWeightFrom : weightFrom) : heightFrom, isWeight ? weightTo : heightTo);
	}

	private boolean inRange(double from, double to) {
		return potentialConverted.getFull().abs().compareTo(BigDecimal.valueOf(from)) > 0 && potentialConverted.getFull().abs().compareTo(BigDecimal.valueOf(to)) < 0;
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
