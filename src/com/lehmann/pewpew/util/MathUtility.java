package com.lehmann.pewpew.util;

/**
 * Utilitário para funções matemáticas
 * 
 * @author limao
 * 
 */
public class MathUtility {

	public static float asFloat(final Object obj) {
		return MathUtility.asFloat(obj, 0f);
	}

	public static float asFloat(final Object obj, final float defvalue) {
		if (obj instanceof Number) {
			return ((Number) obj).floatValue();
		}
		return defvalue;
	}

	public static float toRadians(final float degrees) {
		return (float) (Math.PI / 180) * degrees;
	}

}
