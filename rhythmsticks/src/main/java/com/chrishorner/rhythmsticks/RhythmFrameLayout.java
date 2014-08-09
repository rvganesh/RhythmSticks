package com.chrishorner.rhythmsticks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.FrameLayout;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class RhythmFrameLayout extends FrameLayout {
    public static final int MODE_TOP_LEFT = 1;
    public static final int MODE_BOTTOM_RIGHT = 2;
    public static final int MODE_LEFT_RIGHT = 3;
    public static final int MODE_TOP_BOTTOM = 4;

    private static final int DEFAULT_COLOR = 0xFF0000FF;
    private static final int DEFAULT_SPACING_DP = 16;
    private static final int MASK_SIZE_DP = 256;
    private static final int[] MASK_COLORS = new int[]{0xFFFFFFFF, 0x00FFFFFF, 0x00FFFFFF, 0xFFFFFFFF};
    private static final float[] MASK_COLOR_POS = new float[]{0f, 0.4f, 0.6f, 1f};

    private final Paint linePaint = new Paint();
    private final Paint maskPaint = new Paint();

    private int mode = MODE_LEFT_RIGHT;
    private float[] horizontalPoints;
    private float[] verticalPoints;
    private int spacing;
    private boolean drawUnderContent;
    private int maskLeft;
    private int maskTop;
    private int maskRight;
    private int maskBottom;
    private boolean drawHorizontalLines = true;
    private boolean drawVerticalLines = true;
    private boolean initialized;
    private Bitmap gridBitmap;
    private Canvas gridCanvas;

    public RhythmFrameLayout(Context context) {
        this(context, null);
    }

    public RhythmFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RhythmFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        spacing = dpToPx(DEFAULT_SPACING_DP);
        linePaint.setColor(DEFAULT_COLOR);
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public void setDrawUnderContent(boolean drawUnderContent) {
        this.drawUnderContent = drawUnderContent;
        invalidate();
    }

    public void setMode(int mode) {
        if (mode < MODE_TOP_LEFT || mode > MODE_TOP_BOTTOM) {
            mode = MODE_TOP_LEFT;
        }

        this.mode = mode;
        invalidate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        initialized = false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!drawUnderContent) {
            super.dispatchDraw(canvas);
        }

        if (!initialized) {
            initialize();
        }

        if (drawHorizontalLines) {
            gridCanvas.drawLines(horizontalPoints, 0, horizontalPoints.length, linePaint);
        }

        if (drawVerticalLines) {
            gridCanvas.drawLines(verticalPoints, 0, verticalPoints.length, linePaint);
        }

        if (shouldDrawMask()) {
            gridCanvas.drawRect(maskLeft, maskTop, maskRight, maskBottom, maskPaint);
        }

        canvas.drawBitmap(gridBitmap, 0, 0, null);

        if (drawUnderContent) {
            super.dispatchDraw(canvas);
        }
    }

    private void initialize() {
        if (gridCanvas == null) {
            gridBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            gridCanvas = new Canvas(gridBitmap);
        } else {
            gridCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        switch (mode) {
            case MODE_TOP_LEFT:
                calculateTopLeftPoints();
                break;
            case MODE_BOTTOM_RIGHT:
                calculateBottomRightPoints();
                break;
            case MODE_LEFT_RIGHT:
                calculateLeftRightPoints();
                setupVerticalMask();
                break;
            case MODE_TOP_BOTTOM:
                calculateTopBottomPoints();
                setupHorizontalMask();
                break;
        }

        initialized = true;
    }

    private void calculateTopLeftPoints() {
        int width = getWidth();
        int height = getHeight();

        int verticalLineCount = width / spacing;
        int verticalPointCount = verticalLineCount * 4; //(x0, y0, x1, y1)
        verticalPoints = new float[verticalPointCount];

        for (int i = 0; i < verticalLineCount; i++) {
            int x = spacing + (i * spacing);
            int index = i * 4;
            verticalPoints[index] = x;
            verticalPoints[index + 1] = 0;
            verticalPoints[index + 2] = x;
            verticalPoints[index + 3] = height;
        }

        int horizontalLineCount = height / spacing;
        int horizontalPointCount = horizontalLineCount * 4;
        horizontalPoints = new float[horizontalPointCount];

        for (int i = 0; i < horizontalLineCount; i++) {
            int y = spacing + (i * spacing);
            int index = i * 4;
            horizontalPoints[index] = 0;
            horizontalPoints[index + 1] = y;
            horizontalPoints[index + 2] = width;
            horizontalPoints[index + 3] = y;
        }
    }

    private void calculateBottomRightPoints() {

    }

    private void calculateLeftRightPoints() {
        int width = getWidth();
        int height = getHeight();

        int verticalLineCount = width / spacing;
        int verticalPointCount = verticalLineCount * 4; //(x0, y0, x1, y1)
        verticalPoints = new float[verticalPointCount];

        for (int i = 0; i < verticalLineCount; i++) {
            int x = spacing + (i * spacing);
            int index = i * 4;
            verticalPoints[index] = x;
            verticalPoints[index + 1] = 0;
            verticalPoints[index + 2] = x;
            verticalPoints[index + 3] = height;
        }

        int horizontalLineCount = height / spacing;
        int horizontalPointCount = horizontalLineCount * 4;
        horizontalPoints = new float[horizontalPointCount];

        for (int i = 0; i < horizontalLineCount; i++) {
            int y = spacing + (i * spacing);
            int index = i * 4;
            horizontalPoints[index] = 0;
            horizontalPoints[index + 1] = y;
            horizontalPoints[index + 2] = width;
            horizontalPoints[index + 3] = y;
        }
    }

    private void calculateTopBottomPoints() {

    }

    private void setupVerticalMask() {
        int maskSize = dpToPx(MASK_SIZE_DP);
        maskLeft = (getWidth() / 2) - (maskSize / 2);
        maskTop = 0;
        maskRight = maskLeft + maskSize;
        maskBottom = getHeight();

        LinearGradient maskShader = new LinearGradient(maskLeft, 0, maskRight, 1, MASK_COLORS, MASK_COLOR_POS, Shader.TileMode.CLAMP);
        maskPaint.setShader(maskShader);
    }

    private void setupHorizontalMask() {

    }

    private boolean shouldDrawMask() {
        return mode == MODE_LEFT_RIGHT || mode == MODE_TOP_BOTTOM;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, displayMetrics);
    }
}