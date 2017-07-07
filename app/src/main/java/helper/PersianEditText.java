/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class PersianEditText extends EditText {
    public PersianEditText(Context context) {
        super(context);
        if (!isInEditMode())
            setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
    }

    public PersianEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
    }

    public PersianEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode())
            setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
    }
}

