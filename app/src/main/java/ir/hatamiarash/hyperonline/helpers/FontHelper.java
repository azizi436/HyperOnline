/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;

import org.jetbrains.annotations.NotNull;

public class FontHelper {
	public static final String FontPath = "fonts/sans.ttf";    // font path
	//    public static final String FontPath = "fonts/shabnam.ttf";    // font path
	public static final String SHABNAM = "fonts/sans.ttf";    // font path
	//    public static final String SHABNAM = "fonts/shabnam.ttf";    // font path
	public static final String IRAN_SANS = "fonts/sans.ttf";    // font path
	private static FontHelper instance;
	private static Typeface persianTypeface;                      // typeface
	private static Typeface shabnam;                      // typeface
	private static Typeface iran_sans;                      // typeface
	
	public FontHelper(@NotNull Context context) {
		persianTypeface = Typeface.createFromAsset(context.getAssets(), FontPath);
		shabnam = Typeface.createFromAsset(context.getAssets(), SHABNAM);
		iran_sans = Typeface.createFromAsset(context.getAssets(), IRAN_SANS);
	}
	
	public static synchronized FontHelper getInstance(Context context) {
		if (instance == null)
			instance = new FontHelper(context);
		return instance;
	}
	
	public static synchronized SpannableString getSpannedString(@NotNull Context context, String TEXT) {
		persianTypeface = Typeface.createFromAsset(context.getAssets(), FontPath);
		SpannableString result = new SpannableString(TEXT);
		result.setSpan(new TypefaceSpan(persianTypeface), 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return result;
	}
	
	public Typeface getPersianTextTypeface() {
		return persianTypeface;
	}
	
	public Typeface Shabnam() {
		return shabnam;
	}
	
	public Typeface Iran_Sans() {
		return iran_sans;
	}
}