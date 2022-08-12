package com.wordpress.brancodes.messaging.reactions.unit;

import static com.wordpress.brancodes.messaging.reactions.unit.Unit.*;

public enum BaseUnitType {
	MASS(KG), LENGTH(M); //, TIME, TEMPERATURE, AMOUNT, CURRENT, LUMINOSITY;

	private final Unit normal;

	BaseUnitType(Unit normal) {
		this.normal = normal;
	}

	public Unit getNormalUnit() {
		return normal;
	}

}
