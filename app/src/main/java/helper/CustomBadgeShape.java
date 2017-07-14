/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import berlin.volders.badger.BadgeShape;
import ir.hatamiarash.hyperonline.R;

public class CustomBadgeShape extends BadgeShape {
    
    private final RectF region;
    private final Paint paint;
    
    public CustomBadgeShape(Context context, @FloatRange(from = 0, to = 1) float scale, int gravity) {
        super(scale, 1, gravity);
        region = new RectF();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getBorderColor(context));
        //paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
    }
    
    private static int getBorderColor(Context context) {
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        if (theme.resolveAttribute(R.attr.colorPrimary, typedValue, true))
            return typedValue.data;
        return Color.TRANSPARENT;
    }
    
    @Override
    protected void onDraw(@NonNull Canvas canvas, @NonNull Rect region, @NonNull Paint paint) {
        float extX = region.width() * 0.1f;
        float extY = region.height() * 0.1f;
        this.region.set(region);
        this.region.left -= extX;
        this.region.top -= extY;
        this.region.right += extX;
        this.region.bottom += extY;
        canvas.drawOval(this.region, this.paint);
        this.region.set(region);
        canvas.drawOval(this.region, paint);
    }
}