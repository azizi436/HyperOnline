/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.libraries;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.FormatHelper;
import timber.log.Timber;

public class PersianTextView extends android.support.v7.widget.AppCompatTextView {
	private static final String TAG = PersianTextView.class.getSimpleName();
	private String _font = "null";
	
	public PersianTextView(Context context) {
		this(context, null);
	}
	
	public PersianTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PersianTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTextSize = getTextSize();
		this.parseAttributes(context, attrs);
		this.initialize(context);
	}
	
	private void parseAttributes(Context context, AttributeSet attrs) {
		if (attrs == null)
			return;
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PersianTextView, 0, 0);
		try {
			_font = a.getString(R.styleable.PersianTextView_my_font);
		} catch (Exception e) {
			Timber.w("Unable to parse attributes due to: %s", e.getMessage());
			e.printStackTrace();
		} finally {
			a.recycle();
		}
	}
	
	private void initialize(Context context) {
		//Log.d(TAG, "initialize()");
		if (!isInEditMode())
			if (_font.equals("shabnam"))
				this.setTypeface(FontHelper.getInstance(context).Shabnam());
			else if (_font.equals("iran_sans"))
				this.setTypeface(FontHelper.getInstance(context).Iran_Sans());
			else
				this.setTypeface(FontHelper.getInstance(context).getPersianTextTypeface());
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		if (text != null)
			text = FormatHelper.toPersianNumber(text.toString());
		super.setText(text, type);
	}
	
	public interface OnTextResizeListener {
		void onTextResize(TextView textView, float oldSize, float newSize);
	}
	
	public static final float MIN_TEXT_SIZE = 20;
	private static final String mEllipsis = "...";
	private OnTextResizeListener mTextResizeListener;
	private boolean mNeedsResize = false;
	private float mTextSize;
	private float mMaxTextSize = 0;
	private float mMinTextSize = MIN_TEXT_SIZE;
	private float mSpacingMult = 1.0f;
	private float mSpacingAdd = 0.0f;
	private boolean mAddEllipsis = true;
	
	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		mNeedsResize = true;
		resetTextSize();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			mNeedsResize = true;
		}
	}
	
	public void setOnResizeListener(OnTextResizeListener listener) {
		mTextResizeListener = listener;
	}
	
	@Override
	public void setTextSize(float size) {
		super.setTextSize(size);
		mTextSize = getTextSize();
	}
	
	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		mTextSize = getTextSize();
	}
	
	@Override
	public void setLineSpacing(float add, float mult) {
		super.setLineSpacing(add, mult);
		mSpacingMult = mult;
		mSpacingAdd = add;
	}
	
	public void setMaxTextSize(float maxTextSize) {
		mMaxTextSize = maxTextSize;
		requestLayout();
		invalidate();
	}
	
	public float getMaxTextSize() {
		return mMaxTextSize;
	}
	
	public void setMinTextSize(float minTextSize) {
		mMinTextSize = minTextSize;
		requestLayout();
		invalidate();
	}
	
	public float getMinTextSize() {
		return mMinTextSize;
	}
	
	public void setAddEllipsis(boolean addEllipsis) {
		mAddEllipsis = addEllipsis;
	}
	
	public boolean getAddEllipsis() {
		return mAddEllipsis;
	}
	
	public void resetTextSize() {
		if (mTextSize > 0) {
			super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
			mMaxTextSize = mTextSize;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed || mNeedsResize) {
			int widthLimit = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
			int heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
			resizeText(widthLimit, heightLimit);
		}
		super.onLayout(changed, left, top, right, bottom);
	}
	
	public void resizeText() {
		
		int heightLimit = getHeight() - getPaddingBottom() - getPaddingTop();
		int widthLimit = getWidth() - getPaddingLeft() - getPaddingRight();
		resizeText(widthLimit, heightLimit);
	}
	
	public void resizeText(int width, int height) {
		CharSequence text = getText();
		if (text == null || text.length() == 0 || height <= 0 || width <= 0 || mTextSize == 0)
			return;
		if (getTransformationMethod() != null)
			text = getTransformationMethod().getTransformation(text, this);
		TextPaint textPaint = getPaint();
		float oldTextSize = textPaint.getTextSize();
		float targetTextSize = mMaxTextSize > 0 ? Math.min(mTextSize, mMaxTextSize) : mTextSize;
		int textHeight = getTextHeight(text, textPaint, width, targetTextSize);
		while (textHeight > height && targetTextSize > mMinTextSize) {
			targetTextSize = Math.max(targetTextSize - 2, mMinTextSize);
			textHeight = getTextHeight(text, textPaint, width, targetTextSize);
		}
		if (mAddEllipsis && targetTextSize == mMinTextSize && textHeight > height) {
			TextPaint paint = new TextPaint(textPaint);
			StaticLayout layout = new StaticLayout(text, paint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
			if (layout.getLineCount() > 0) {
				int lastLine = layout.getLineForVertical(height) - 1;
				if (lastLine < 0)
					setText("");
				
				else {
					int start = layout.getLineStart(lastLine);
					int end = layout.getLineEnd(lastLine);
					float lineWidth = layout.getLineWidth(lastLine);
					float ellipseWidth = textPaint.measureText(mEllipsis);
					while (width < lineWidth + ellipseWidth) {
						lineWidth = textPaint.measureText(text.subSequence(start, --end + 1).toString());
					}
					setText(text.subSequence(0, end) + mEllipsis);
				}
			}
		}
		setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
		setLineSpacing(mSpacingAdd, mSpacingMult);
		if (mTextResizeListener != null)
			mTextResizeListener.onTextResize(this, oldTextSize, targetTextSize);
		mNeedsResize = false;
	}
	
	private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
		TextPaint paintCopy = new TextPaint(paint);
		paintCopy.setTextSize(textSize);
		StaticLayout layout = new StaticLayout(source, paintCopy, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
		return layout.getHeight();
	}
}