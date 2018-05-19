/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.libraries;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.helpers.FontHelper;

public class IconEditText extends LinearLayout {
	private static final String TAG = IconEditText.class.getSimpleName();
	private static final float ICON_WEIGHT = 0.15f;
	private static final float EDIT_TEXT_WEIGHT = 0.85f;
	private static final String HINT_PREFIX = " ";
	private Integer _iconResource;
	private String _hint;
	private boolean _isPassword = false;
	private boolean _isPhone = false;
	private boolean _isName = false;
	private boolean _isEmail = false;
	private boolean _isAddress = false;
	private ImageView _icon;
	private PersianEditText _editText;
	
	public IconEditText(Context context) {
		this(context, null);
	}
	
	public IconEditText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public IconEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.parseAttributes(context, attrs);
		this.initialize();
	}
	
	private void parseAttributes(Context context, AttributeSet attrs) {
		Log.d(TAG, "parseAttributes()");
		if (attrs == null)
			return;
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IconEditText, 0, 0);
		try {
			_iconResource = a.getResourceId(R.styleable.IconEditText_iconSrc, 0);
			_hint = a.getString(R.styleable.IconEditText_hint);
			_isPassword = a.getBoolean(R.styleable.IconEditText_isPassword, false);
			_isPhone = a.getBoolean(R.styleable.IconEditText_isPhone, false);
			_isEmail = a.getBoolean(R.styleable.IconEditText_isEmail, false);
			_isAddress = a.getBoolean(R.styleable.IconEditText_isAddress, false);
			_isName = a.getBoolean(R.styleable.IconEditText_isName, false);
			//Log.d(TAG, "{ _iconResource: " + _iconResource + ", _hint: " + _hint + ", _isPassword: " + _isPassword + "}");
		} catch (Exception e) {
			Log.e(TAG, "Unable to parse attributes due to: " + e.getMessage());
			e.printStackTrace();
		} finally {
			a.recycle();
		}
	}
	
	private void initialize() {
		Log.d(TAG, "initialize()");
		this.setOrientation(LinearLayout.HORIZONTAL);
		if (_editText == null) {
			_editText = new PersianEditText(this.getContext());
			
			if (_isPassword) {
				_editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				_editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				_editText.setKeyListener(DigitsKeyListener.getInstance("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"));
			} else if (_isPhone)
				_editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);
			else if (_isEmail)
				_editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			else if (_isAddress)
				_editText.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			else if (_isName)
				_editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			
			_editText.setMaxLines(1);
			_editText.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			_editText.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, EDIT_TEXT_WEIGHT));
			
			if (_hint != null)
				_editText.setHint(String.format("%s%s", HINT_PREFIX, _hint.toLowerCase()));
			
			_editText.setBackgroundDrawable(null);
			_editText.setTextColor(ContextCompat.getColor(this.getContext(), R.color.white));
			_editText.setHintTextColor(ContextCompat.getColor(this.getContext(), R.color.gray));
			_editText.setTransformationMethod(new SingleLineTransformationMethod());
			this.addView(_editText);
		}
		if (_icon == null) {
			_icon = new ImageView(this.getContext());
			_icon.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, ICON_WEIGHT));
			_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			if (_iconResource != null && _iconResource != 0) {
				_icon.setImageResource(_iconResource);
				_icon.setColorFilter(getResources().getColor(R.color.white));
			}
			this.addView(_icon);
		}
	}
	
	public void setError(Context context, String error) {
		_editText.setError(FontHelper.getSpannedString(context, error));
	}
	
	public void setText(Context context, String text) {
		_editText.setText(FontHelper.getSpannedString(context, text));
	}
	
	public Editable getText() {
		return _editText.getText();
	}
	
	public EditText getEditText() {
		return _editText;
	}
	
	public ImageView getImageView() {
		return _icon;
	}
}