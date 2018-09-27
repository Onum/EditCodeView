package com.bigbangbutton.editcodeview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class EditCodeView extends View
        implements View.OnClickListener, View.OnFocusChangeListener {

    private static final int DEFAULT_CODE_LENGTH = 4;
    private static final String DEFAULT_CODE_MASK = "*";
    private static final String DEFAULT_CODE_SYMBOL = "0";
    private static final String DEFAULT_REGEX = "[^0-9]";
    private static final float DEFAULT_REDUCTION_SCALE = 0.5f;

    private final CodeTextWatcher textWatcher = new CodeTextWatcher();
    private InputMethodManager inputmethodmanager;
    private EditCodeInputConnection editCodeInputConnection;
    private EditCodeListener editCodeListener;
    private EditCodeWatcher editCodeWatcher;
    private Editable editable;

    private Paint textPaint;
    private Paint underlinePaint;
    private Paint cursorPaint;

    private float textSize;
    private float textPosY;
    private int textColor;
    private float sectionWidth;
    private int codeLength;
    private float symbolWidth;
    private float symbolMaskedWidth;
    private float underlineHorizontalPadding;
    private float underlineReductionScale;
    private float underlineStrokeWidth;
    private int underlineBaseColor;
    private int underlineSelectedColor;
    private int underlineFilledColor;
    private int underlineCursorColor;
    private float underlinePosY;
    private int fontStyle;
    private boolean cursorEnabled;
    private boolean codeHiddenMode;
    private boolean isSelected;
    private String codeHiddenMask;
    private Rect textBounds = new Rect();

    private Runnable cursorAnimation = new Runnable() {
        public void run() {
            int color = cursorPaint.getColor() == underlineSelectedColor
                    ? underlineCursorColor
                    : underlineSelectedColor;
            cursorPaint.setColor(color);
            invalidate();
            postDelayed(cursorAnimation, 500);
        }
    };

    public EditCodeView(Context context) {
        super(context);
        init(context, null);
    }

    public EditCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        initDefaultAttrs(context);
        initCustomAttrs(context, attrs);
        initPaints();
        initViewsOptions(context);

        if (isInEditMode()) {
            editModePreview();
        }
    }

    private void initDefaultAttrs(Context context) {
        Resources resources = context.getResources();

        underlineReductionScale = DEFAULT_REDUCTION_SCALE;
        underlineStrokeWidth = resources.getDimension(R.dimen.underline_stroke_width);
        underlineBaseColor = ContextCompat.getColor(context, R.color.underline_base_color);
        underlineFilledColor = ContextCompat.getColor(context, R.color.underline_filled_color);
        underlineCursorColor = ContextCompat.getColor(context, R.color.underline_cursor_color);
        underlineSelectedColor = ContextCompat.getColor(context, R.color.underline_selected_color);
        textSize = resources.getDimension(R.dimen.code_text_size);
        textColor = ContextCompat.getColor(context, R.color.text_main_color);
        codeLength = DEFAULT_CODE_LENGTH;
        codeHiddenMask = DEFAULT_CODE_MASK;
    }

    private void initCustomAttrs(Context context, AttributeSet attributeSet) {
        if (attributeSet == null) return;

        TypedArray attributes = context.obtainStyledAttributes(
                attributeSet, R.styleable.EditCodeView);

        underlineStrokeWidth = attributes.getDimension(
                R.styleable.EditCodeView_underlineStroke, underlineStrokeWidth);

        underlineReductionScale = attributes.getFloat(
                R.styleable.EditCodeView_underlineReductionScale, underlineReductionScale);

        underlineBaseColor = attributes.getColor(
                R.styleable.EditCodeView_underlineBaseColor, underlineBaseColor);

        underlineSelectedColor = attributes.getColor(
                R.styleable.EditCodeView_underlineSelectedColor, underlineSelectedColor);

        underlineFilledColor = attributes.getColor(
                R.styleable.EditCodeView_underlineFilledColor, underlineFilledColor);

        underlineCursorColor = attributes.getColor(
                R.styleable.EditCodeView_underlineCursorColor, underlineCursorColor);

        cursorEnabled = attributes.getBoolean(
                R.styleable.EditCodeView_underlineCursorEnabled, cursorEnabled);

        textSize = attributes.getDimension(
                R.styleable.EditCodeView_textSize, textSize);

        textColor = attributes.getColor(
                R.styleable.EditCodeView_textColor, textColor);

        fontStyle = attributes.getInt(
                R.styleable.EditCodeView_font_style, fontStyle);

        codeLength = attributes.getInt(
                R.styleable.EditCodeView_codeLength, DEFAULT_CODE_LENGTH);

        codeHiddenMode = attributes.getBoolean(
                R.styleable.EditCodeView_codeHiddenMode, codeHiddenMode);

        String mask = attributes.getString(R.styleable.EditCodeView_codeHiddenMask);
        if (mask != null && mask.length() > 0) {
            codeHiddenMask = mask.substring(0, 1);
        }

        attributes.recycle();
    }

    private void editModePreview() {
        for (int i = 0; i < codeLength; i++) {
            if (codeHiddenMode) {
                editable.append(codeHiddenMask);
            } else {
                editable.append(DEFAULT_CODE_SYMBOL);
            }
        }
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, fontStyle));
        textPaint.setAntiAlias(true);

        underlinePaint = new Paint();
        underlinePaint.setColor(underlineBaseColor);
        underlinePaint.setStrokeWidth(underlineStrokeWidth);

        cursorPaint = new Paint();
        cursorPaint.setColor(underlineBaseColor);
        cursorPaint.setStrokeWidth(underlineStrokeWidth);
    }

    private void initViewsOptions(Context context) {
        setOnClickListener(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnFocusChangeListener(this);

        inputmethodmanager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        editable = Editable.Factory.getInstance().newEditable("");
        editable.setSpan(textWatcher, 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        Selection.setSelection(editable, 0);

        editCodeInputConnection = new EditCodeInputConnection(this, true, codeLength);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measureSizes(w, h);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawUnderline(canvas);
        drawText(canvas);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        setSelected(hasFocus);
        if (hasFocus) {
            if (cursorEnabled) {
                post(cursorAnimation);
            }
            showKeyboard();
        } else {
            if (cursorEnabled) {
                removeCallbacks(cursorAnimation);
            }
            hideKeyboard();
        }
    }

    private void drawText(Canvas canvas) {
        if (codeHiddenMode) {
            char[] symbol = {codeHiddenMask.charAt(0)};
            for (int i = 0; i < editable.length(); i++) {
                float textPosX = sectionWidth * i + sectionWidth / 2 - symbolMaskedWidth / 2;
                canvas.drawText(symbol, 0, 1, textPosX, textPosY, textPaint);
            }
        } else {
            for (int i = 0; i < editable.length(); i++) {
                char[] symbol = {editable.charAt(i)};
                float textPosX = sectionWidth * i + sectionWidth / 2 - symbolWidth / 2;
                canvas.drawText(symbol, 0, 1, textPosX, textPosY, textPaint);
            }
        }
    }

    private void drawUnderline(Canvas canvas) {
        for (int i = 0; i < codeLength; i++) {
            float startPosX = sectionWidth * i + underlineHorizontalPadding;
            float endPosX = startPosX + sectionWidth - underlineHorizontalPadding * 2;

            if (cursorEnabled && isSelected && editable.length() == i) {
                canvas.drawLine(startPosX, underlinePosY, endPosX, underlinePosY, cursorPaint);
            } else {
                if (editable.length() <= i && isSelected) {
                    underlinePaint.setColor(underlineSelectedColor);
                } else if (editable.length() <= i && !isSelected) {
                    underlinePaint.setColor(underlineBaseColor);
                } else {
                    underlinePaint.setColor(underlineFilledColor);
                }
                canvas.drawLine(startPosX, underlinePosY, endPosX, underlinePosY, underlinePaint);
            }
        }
    }

    private void measureSizes(int viewWidth, int viewHeight) {
        if (underlineReductionScale > 1) underlineReductionScale = 1;
        if (underlineReductionScale < 0) underlineReductionScale = 0;

        if (codeLength <= 0) {
            throw new IllegalArgumentException("Code length must be over than zero");
        }

        symbolWidth = textPaint.measureText(DEFAULT_CODE_SYMBOL);
        symbolMaskedWidth = textPaint.measureText(codeHiddenMask);
        textPaint.getTextBounds(DEFAULT_CODE_SYMBOL, 0, 1, textBounds);
        sectionWidth = viewWidth / codeLength;
        underlinePosY = viewHeight - getPaddingBottom();
        underlineHorizontalPadding = sectionWidth * underlineReductionScale / 2;
        textPosY = viewHeight / 2 + textBounds.height() / 2;
    }

    private int measureHeight(int measureSpec) {
        int size = (int) (getPaddingBottom()
                + getPaddingTop()
                + textBounds.height()
                + textSize
                + underlineStrokeWidth);
        return resolveSizeAndState(size, measureSpec, 0);
    }

    private int measureWidth(int measureSpec) {
        int size = (int) ((getPaddingLeft() + getPaddingRight() + textSize) * codeLength * 2);
        return resolveSizeAndState(size, measureSpec, 0);
    }

    public void setEditCodeListener(EditCodeListener EditCodeListener) {
        this.editCodeListener = EditCodeListener;
    }

    public void setCode(@NonNull String code) {
        code = code.replaceAll(DEFAULT_REGEX, "");
        editCodeInputConnection.setComposingText(code, 1);
        editCodeInputConnection.finishComposingText();
    }

    public void clearCode() {
        editCodeInputConnection.setComposingRegion(0, codeLength);
        editCodeInputConnection.setComposingText("", 0);
        editCodeInputConnection.finishComposingText();
    }

    public String getCode() {
        return editable.toString();
    }

    public void setReductionScale(float scale) {
        if (scale > 1) scale = 1;
        if (scale < 0) scale = 0;

        underlineReductionScale = scale;
        invalidate();
    }

    public void setCodeHiddenMode(boolean hiddenMode) {
        codeHiddenMode = hiddenMode;
        invalidate();
    }

    public void setUnderlineBaseColor(@ColorInt int colorId) {
        underlineBaseColor = colorId;
        invalidate();
    }

    public void setUnderlineFilledColor(@ColorInt int colorId) {
        underlineFilledColor = colorId;
        invalidate();
    }

    public void setUnderlineSelectedColor(@ColorInt int colorId) {
        underlineSelectedColor = colorId;
        invalidate();
    }

    public void setUnderlineCursorColor(@ColorInt int colorId) {
        underlineCursorColor = colorId;
        invalidate();
    }

    public void setTextColor(@ColorInt int colorId) {
        textColor = colorId;
        invalidate();
    }

    public void setUnderlineStrokeWidth(float underlineStrokeWidth) {
        this.underlineStrokeWidth = underlineStrokeWidth;
        invalidate();
    }

    public void setCodeLength(int length) {
        codeLength = length;
        editCodeInputConnection = new EditCodeInputConnection(this, true, codeLength);
        editable.clear();
        inputmethodmanager.restartInput(this);
        invalidate();
    }

    public void setEditCodeWatcher(EditCodeWatcher editCodeWatcher) {
        this.editCodeWatcher = editCodeWatcher;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void showKeyboard() {
        inputmethodmanager.showSoftInput(this, 0);
    }

    public void hideKeyboard() {
        inputmethodmanager.hideSoftInputFromWindow(
                getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    @Override
    public void onClick(View v) {
        showKeyboard();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        outAttrs.initialSelStart = 0;

        return editCodeInputConnection;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        invalidate();
    }

    protected Editable getEditable() {
        return editable;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    private class CodeTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            invalidate();
            if(editCodeWatcher != null) {
                editCodeWatcher.onCodeChanged(s.toString());
            }
            if (editable.length() == codeLength) {
                if (editCodeListener != null) {
                    editCodeListener.onCodeReady(editable.toString());
                }
            }
        }
    }
}
