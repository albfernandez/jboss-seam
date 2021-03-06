package org.jboss.seam.pdf;

import java.awt.Color;

import org.jboss.seam.util.Strings;

import com.lowagie.text.ElementTags;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.WebColors;
import com.lowagie.text.pdf.PdfWriter;

public class ITextUtils {
	

	/**
	* not all itext objects accept a string value as input, so we'll copy that
	* logic here.
	*/
	public static int alignmentValue(String alignment) {
		return ElementTags.alignmentValue(alignment);
	}

	public static Rectangle pageSizeValue(String name) {
		return PageSize.getRectangle(name);
	}

	/**
	* return a color value from a string specification.
	*/
	public static Color colorValue(String colorName) {
		if (Strings.isEmpty(colorName)) {
			return null;
		}
		
		String name = colorName.trim().toLowerCase();
		Color color = null;
		if (name.startsWith("rgb") || name.startsWith("hsl") || name.startsWith("#")) {
			color = rgbStringToColor(name);
		}
		if (color == null) {
			try {
				color = Color.decode(name);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		return color;
	}

	/*
	* Returns color of the form rgb(r,g,b) or rgb(r,g,b,a) r,g,b,a values can be
	* 0-255 or float values with a '%' sign
	*/
	public static Color rgbStringToColor(String rgbString) {
		try {
			return WebColors.getRGBColor(rgbString);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}


	public static float[] stringToFloatArray(String text) {
		String[] parts = text.split("\\s");
		float[] values = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			values[i] = Float.parseFloat(parts[i]);
		}

		return values;
	}

	public static int[] stringToIntArray(String text) {
		String[] parts = text.split("\\s");
		int[] values = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			values[i] = Integer.parseInt(parts[i]);
		}

		return values;
	}

	public static int runDirection(String direction) {
		if (direction == null || "default".equalsIgnoreCase(direction)) {
			return PdfWriter.RUN_DIRECTION_DEFAULT;
		} else if ("rtl".equalsIgnoreCase(direction)) {
			return PdfWriter.RUN_DIRECTION_RTL;
		} else if ("ltr".equalsIgnoreCase(direction)) {
			return PdfWriter.RUN_DIRECTION_LTR;
		} else if ("no-bidi".equalsIgnoreCase(direction)) {
			return PdfWriter.RUN_DIRECTION_NO_BIDI;
		} else {
			throw new RuntimeException("unknown run direction " + direction);
		}
	}
}
