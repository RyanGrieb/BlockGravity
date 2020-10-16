package me.rhin.blockgravity.util;

public class MathUtil {

	public static double clampDouble(double value, double min, double max) {
		if (value < min)
			value = min;
		if (value > max)
			value = max;

		return value;
	}

	public static int clampInt(int value, int min, int max) {
		if (value < min)
			value = min;
		if (value > max)
			value = max;

		return value;
	}
}
