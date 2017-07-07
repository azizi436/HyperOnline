/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.MyToast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import helper.FontHelper;

@SuppressLint({"InflateParams"})
public class CustomToast {
    @ColorInt
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#FFFFFF");
    @ColorInt
    private static final int ERROR_COLOR = Color.parseColor("#D50000");
    @ColorInt
    private static final int INFO_COLOR = Color.parseColor("#3F51B5");
    @ColorInt
    private static final int SUCCESS_COLOR = Color.parseColor("#388E3C");
    @ColorInt
    private static final int WARNING_COLOR = Color.parseColor("#FFA900");
    private static final String TOAST_TYPEFACE = "sans-serif-condensed";
    
    private CustomToast() {
    }
    
    @CheckResult
    public static Toast normal(@NonNull Context context, @NonNull String message) {
        return normal(context, message, 0, (Drawable) null, false);
    }
    
    @CheckResult
    public static Toast normal(@NonNull Context context, @NonNull String message, Drawable icon) {
        return normal(context, message, 0, icon, true);
    }
    
    @CheckResult
    public static Toast normal(@NonNull Context context, @NonNull String message, int duration) {
        return normal(context, message, duration, (Drawable) null, false);
    }
    
    @CheckResult
    public static Toast normal(@NonNull Context context, @NonNull String message, int duration, Drawable icon) {
        return normal(context, message, duration, icon, true);
    }
    
    @CheckResult
    public static Toast normal(@NonNull Context context, @NonNull String message, int duration, Drawable icon, boolean withIcon) {
        return custom(context, message, icon, DEFAULT_TEXT_COLOR, duration, withIcon);
    }
    
    @CheckResult
    public static Toast warning(@NonNull Context context, @NonNull String message) {
        return warning(context, message, 0, true);
    }
    
    @CheckResult
    public static Toast warning(@NonNull Context context, @NonNull String message, int duration) {
        return warning(context, message, duration, true);
    }
    
    @CheckResult
    public static Toast warning(@NonNull Context context, @NonNull String message, int duration, boolean withIcon) {
        return custom(context, message, CustomToastUtils.getDrawable(context, es.dmoral.toasty.R.drawable.ic_error_outline_white_48dp), DEFAULT_TEXT_COLOR, WARNING_COLOR, duration, withIcon, true);
    }
    
    @CheckResult
    public static Toast info(@NonNull Context context, @NonNull String message) {
        return info(context, message, 0, true);
    }
    
    @CheckResult
    public static Toast info(@NonNull Context context, @NonNull String message, int duration) {
        return info(context, message, duration, true);
    }
    
    @CheckResult
    public static Toast info(@NonNull Context context, @NonNull String message, int duration, boolean withIcon) {
        return custom(context, message, CustomToastUtils.getDrawable(context, es.dmoral.toasty.R.drawable.ic_info_outline_white_48dp), DEFAULT_TEXT_COLOR, INFO_COLOR, duration, withIcon, true);
    }
    
    @CheckResult
    public static Toast success(@NonNull Context context, @NonNull String message) {
        return success(context, message, 0, true);
    }
    
    @CheckResult
    public static Toast success(@NonNull Context context, @NonNull String message, int duration) {
        return success(context, message, duration, true);
    }
    
    @CheckResult
    public static Toast success(@NonNull Context context, @NonNull String message, int duration, boolean withIcon) {
        return custom(context, message, CustomToastUtils.getDrawable(context, es.dmoral.toasty.R.drawable.ic_check_white_48dp), DEFAULT_TEXT_COLOR, SUCCESS_COLOR, duration, withIcon, true);
    }
    
    @CheckResult
    public static Toast error(@NonNull Context context, @NonNull String message) {
        return error(context, message, 0, true);
    }
    
    @CheckResult
    public static Toast error(@NonNull Context context, @NonNull String message, int duration) {
        return error(context, message, duration, true);
    }
    
    @CheckResult
    public static Toast error(@NonNull Context context, @NonNull String message, int duration, boolean withIcon) {
        return custom(context, message, CustomToastUtils.getDrawable(context, es.dmoral.toasty.R.drawable.ic_clear_white_48dp), DEFAULT_TEXT_COLOR, ERROR_COLOR, duration, withIcon, true);
    }
    
    @CheckResult
    public static Toast custom(@NonNull Context context, @NonNull String message, Drawable icon, @ColorInt int textColor, int duration, boolean withIcon) {
        return custom(context, message, icon, textColor, -1, duration, withIcon, false);
    }
    
    @CheckResult
    public static Toast custom(@NonNull Context context, @NonNull String message, @DrawableRes int iconRes, @ColorInt int textColor, @ColorInt int tintColor, int duration, boolean withIcon, boolean shouldTint) {
        return custom(context, message, CustomToastUtils.getDrawable(context, iconRes), textColor, tintColor, duration, withIcon, shouldTint);
    }
    
    @CheckResult
    public static Toast custom(@NonNull Context context, @NonNull String message, Drawable icon, @ColorInt int textColor, @ColorInt int tintColor, int duration, boolean withIcon, boolean shouldTint) {
        Toast currentToast = new Toast(context);
        View toastLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(es.dmoral.toasty.R.layout.toast_layout, (ViewGroup) null);
        ImageView toastIcon = (ImageView) toastLayout.findViewById(es.dmoral.toasty.R.id.toast_icon);
        TextView toastTextView = (TextView) toastLayout.findViewById(es.dmoral.toasty.R.id.toast_text);
        Drawable drawableFrame;
        if (shouldTint) {
            drawableFrame = CustomToastUtils.tint9PatchDrawableFrame(context, tintColor);
        } else {
            drawableFrame = CustomToastUtils.getDrawable(context, es.dmoral.toasty.R.drawable.toast_frame);
        }

        CustomToastUtils.setBackground(toastLayout, drawableFrame);
        if (withIcon) {
            if (icon == null) {
                throw new IllegalArgumentException("Avoid passing \'icon\' as null if \'withIcon\' is set to true");
            }

            CustomToastUtils.setBackground(toastIcon, icon);
        } else {
            toastIcon.setVisibility(View.INVISIBLE);
        }

        toastTextView.setTextColor(textColor);
        toastTextView.setText(FontHelper.getSpannedString(context, message));
        toastTextView.setTypeface(Typeface.create("sans-serif-condensed", 0));
        currentToast.setView(toastLayout);
        currentToast.setDuration(duration);
        return currentToast;
    }
}