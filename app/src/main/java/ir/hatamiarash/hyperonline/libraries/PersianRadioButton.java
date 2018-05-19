/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.libraries;

import android.content.Context;
import android.util.AttributeSet;

import ir.hatamiarash.hyperonline.helpers.FontHelper;

public class PersianRadioButton extends android.support.v7.widget.AppCompatRadioButton {
	public PersianRadioButton(Context context) {
		super(context);
		if (!isInEditMode())
			setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
	}
	
	public PersianRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode())
			setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
	}
	
	public PersianRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode())
			setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
	}
}

