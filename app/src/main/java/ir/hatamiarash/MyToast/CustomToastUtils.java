/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.MyToast;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.View;


final class CustomToastUtils {
	private CustomToastUtils() {
	}
	
	static {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}
	
	static Drawable tint9PatchDrawableFrame(@NonNull Context context, @ColorInt int tintColor) {
		NinePatchDrawable toastDrawable = (NinePatchDrawable) getDrawable(context, es.dmoral.toasty.R.drawable.toast_frame);
		toastDrawable.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
		return toastDrawable;
	}
	
	static void setBackground(@NonNull View view, Drawable drawable) {
		if (Build.VERSION.SDK_INT >= 16)
			view.setBackground(drawable);
		else
			view.setBackgroundDrawable(drawable);
	}
	
	static Drawable getDrawable(@NonNull Context context, @DrawableRes int id) {
		return AppCompatDrawableManager.get().getDrawable(context, id);
	}
}