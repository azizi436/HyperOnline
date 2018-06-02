/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FormatHelper {
	private static String[] persianNumbers = new String[]{"۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"};
	
	@NonNull
	public static String toPersianNumber(@NotNull String text) {
		if (text.isEmpty())
			return "";
		StringBuilder out = new StringBuilder();
		int length = text.length();
		for (int i = 0; i < length; i++) {
			char c = text.charAt(i);
			if ('0' <= c && c <= '9') {
				int number = Integer.parseInt(String.valueOf(c));
				out.append(persianNumbers[number]);
			} else if (c == '٫') {
				out.append('،');
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	@Contract(pure = true)
	public static String toEnglishNumber(@NotNull String c) {
		String out;
		switch (c) {
			case "۰":
				out = "0";
				break;
			case "۱":
				out = "1";
				break;
			case "۲":
				out = "2";
				break;
			case "۳":
				out = "3";
				break;
			case "۴":
				out = "4";
				break;
			case "۵":
				out = "5";
				break;
			case "۶":
				out = "6";
				break;
			case "۷":
				out = "7";
				break;
			case "۸":
				out = "8";
				break;
			case "۹":
				out = "9";
				break;
			default:
				out = c;
				break;
		}
		return out;
	}
	
	@Contract(pure = true)
	@NotNull
	public static String fixResponse(@NotNull String response) {
		return "{\"" + response.substring(response.indexOf("error"));
	}
	
	@NonNull
	public static String formatDate(@NotNull String Date) {
		String[] split = Date.split(":");
		return split[0] + "     " + split[1];
	}
	
	@NonNull
	public static String formatCardNumber(@NotNull String card) {
		return card.replaceAll("....", "$0-").substring(0, card.length() + 3);
	}
}
