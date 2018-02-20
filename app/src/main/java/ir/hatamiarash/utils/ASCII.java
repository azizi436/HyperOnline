/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.utils;

import org.jetbrains.annotations.Contract;

public class ASCII {
	public static boolean isASCII(String s) {
		for (int i = 0; i < s.length(); i++)
			if (!isLetterOrNumber(s.charAt(i)))
				return false;
		return true;
	}
	
	private static boolean isLetterOrNumber(int c) {
		return isLetter(c) || isNumber(c);
	}
	
	private static boolean isLetter(int c) {
		return isUpperCaseLetter(c) || isLowerCaseLetter(c);
	}
	
	@Contract(pure = true)
	private static boolean isUpperCaseLetter(int c) {
		return (c >= 65 && c <= 90); // A - Z
	}
	
	@Contract(pure = true)
	private static boolean isLowerCaseLetter(int c) {
		return (c >= 97 && c <= 122);  // a - z
	}
	
	@Contract(pure = true)
	private static boolean isNumber(int c) {
		return (c >= 48 && c <= 57); // 0 - 9
	}
}