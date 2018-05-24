/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpan extends MetricAffectingSpan {
	private Typeface mTypeface;
	
	TypefaceSpan(Typeface typeface) {
		mTypeface = typeface;
	}
	
	@Override
	public void updateMeasureState(TextPaint p) {
		p.setTypeface(mTypeface);
		p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}
	
	@Override
	public void updateDrawState(TextPaint tp) {
		tp.setTypeface(mTypeface);
		tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}
}
