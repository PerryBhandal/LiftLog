package com.plined.liftlog;

public class StringFormatter {
	
	/*
	 * Returns a string containing the weight. Omits the decimal if there isn't one.
	 * 
	 * weightToString(800.0) -> "800"
	 * weightToString(800.5) -> "800.5"
	 */
	public static String weightToString(float weight) {
		if (weight % 1 != 0) {
			//It has decimals.
			return String.format("%s", weight);
		} else {
			//It doesn't have decimals.
			return String.format("%d", (int)weight);
		}
	}
	
}
