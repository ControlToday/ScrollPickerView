package com.expand.widget.scrollPicker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.expand.widget.R;

public class ScrollPickerView extends TextView {
    private static final int DEFAULT_TEXT_SIZE_MAX = 40;
    private static final int DEFAULT_TEXT_SIZE_MIN = 20;
    private static final int DEFAULT_TEXT_LINES = 3;
    private static final int DEFAULT_COLOR = 0XFFFF0000;

    // 在效果图中,最下文本的下划线总是不可见,为了达到这种效果,整体view的绘制下移VIEW_OFFSET
    private static final int VIEW_OFFSET = 3;

    private float textSizeMax;
    private float textSizeMin;
    private int textLines;
    private int textColorDefault;
    private int textColorSelected;
    private int divideColorDefault;
    private int divideColorSelected;
    private float lineHeight;

    Paint paint;
    ValueAnimator animator;
    VelocityTracker velocityTracker;
    /**
     * 默认的adapter,用于xml预览颜色时使用
     */
    Adapter adapter = new Adapter() {
        @Override
        public int getCount() {
            return textLines;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public String getString(int position) {
            return "测试数据" + position;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    };
    OnSelectedIndexChangedListener onSelectedIndexChangedListener;
    /**
     * 被选中项的索引
     */
    private int currentSelectIndex = 0;

    /**
     * 相对于选中项的偏移量
     */
    private float relativeCurrentSelectOffset;
    /**
     * 手指滑动过程中,所在的位置,辅助滑动事件
     */
    private int lastTouchPositionPixel;

    public ScrollPickerView(Context context) {
        this(context, null);
    }

    public ScrollPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFieldValues(context, attrs, defStyleAttr);
        initPaint();
        initVelocityTracker();
    }

    private void initFieldValues(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScrollPickerView,
                defStyleAttr, 0);
        textSizeMax = array.getDimensionPixelSize(R.styleable.ScrollPickerView_text_size_max,
                DEFAULT_TEXT_SIZE_MAX);
        textSizeMin = array.getDimensionPixelSize(R.styleable.ScrollPickerView_text_size_min,
                DEFAULT_TEXT_SIZE_MIN);
        textLines = array.getInt(R.styleable.ScrollPickerView_text_lines, DEFAULT_TEXT_LINES);
        if (textLines < 3)
            textLines = 3;
        if (textLines % 2 == 0)
            textLines++;
        textColorDefault = array.getColor(R.styleable.ScrollPickerView_text_color_default,
                DEFAULT_COLOR);
        textColorSelected = array.getColor(R.styleable.ScrollPickerView_text_color_selected,
                DEFAULT_COLOR);
        divideColorDefault = array.getColor(R.styleable.ScrollPickerView_divide_color_default,
                textColorDefault);
        divideColorSelected = array.getColor(R.styleable.ScrollPickerView_divide_color_selected,
                textColorSelected);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getTextSize());
        paint.setColor(textColorDefault);
        paint.setAntiAlias(true);
    }

    private void initVelocityTracker() {
        velocityTracker = VelocityTracker.obtain();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        lineHeight = getMeasuredHeight() / textLines;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!adapter.isEmpty())
            for (int i = currentSelectIndex - textLines / 2;
                 i <= currentSelectIndex + textLines / 2; i++) {
                if (i >= 0 && i < adapter.getCount())
                    drawTextAndLine(canvas, adapter.getString(i), i);
            }
    }

    private void drawTextAndLine(Canvas canvas, String text, int position) {
        Paint paint = getTextPaint(position);
        int horizontalCenter = getMeasuredWidth() / 2;
        float top = lineHeight * (textLines / 2 + position - currentSelectIndex) + relativeCurrentSelectOffset
                + VIEW_OFFSET;
        float bottom = top + lineHeight;
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        float baseline = (bottom + top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(text, horizontalCenter, baseline, paint);

        Paint paint1 = getDividePaint(position);
        canvas.drawLine(0, bottom, getMeasuredWidth(), bottom, paint1);
    }

    private Paint getTextPaint(int position) {
        if (position == currentSelectIndex)
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        else
            paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(getTextColor(position));
        paint.setTextSize(getTextSize(position));
        return paint;
    }

    private Paint getDividePaint(int position) {
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(getDivideColor(position));
        paint.setStrokeWidth(1);
        return paint;
    }

    private int getTextColor(int position) {
        if (position == currentSelectIndex)
            return textColorSelected;
        return textColorDefault;
    }

    private int getDivideColor(int position) {
        if (position == currentSelectIndex)
            return divideColorSelected;
        return divideColorDefault;
    }

    private float getTextSize(int position) {
        float offsetPex;
        if (position == currentSelectIndex) {
            offsetPex = Math.abs(relativeCurrentSelectOffset);
        } else if (position < currentSelectIndex)
            offsetPex = Math.abs(position - currentSelectIndex) * lineHeight - relativeCurrentSelectOffset;
        else
            offsetPex = Math.abs(position - currentSelectIndex) * lineHeight + relativeCurrentSelectOffset;
        float textSize = textSizeMax -
                (textSizeMax - textSizeMin) * offsetPex / (getMeasuredHeight() / 2);
        return textSize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchPositionPixel = (int) event.getY();
                cancleAnimator();
                break;
            case MotionEvent.ACTION_MOVE:
                float offset = event.getY() - lastTouchPositionPixel;
                lastTouchPositionPixel = (int) event.getY();
                postOffset(offset);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.computeCurrentVelocity(50, 2 * lineHeight);
                int yVelocity = (int) velocityTracker.getYVelocity();
                Log.e("100毫秒的平均速度", "" + yVelocity);
                inertiaMove(yVelocity);
//                adjustAtInertiaMoveEnd();
                break;
        }
        return true;
    }

    // 第一次调整,惯性滑动,滑动的速度慢慢下降
    private void inertiaMove(final int speed) {
        cancleAnimator();
        if (Math.abs(speed) < lineHeight) {
            adjustAtInertiaMoveEnd();
            return;
        }
        animator = ValueAnimator.ofInt(speed / 2, 0);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curValue = (int) animation.getAnimatedValue();
                postOffset(curValue);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                adjustAtInertiaMoveEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    // 第二次调整,当速度降到***时,进行二次调整
    private void adjustAtInertiaMoveEnd() {
        cancleAnimator();
        if (relativeCurrentSelectOffset == 0)
            return;
        animator = ValueAnimator.ofFloat(relativeCurrentSelectOffset, 0);
        animator.setDuration(100);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curValue = (float) animation.getAnimatedValue();
                relativeCurrentSelectOffset = curValue;
                invalidate();
            }
        });
        animator.start();
    }

    private void cancleAnimator() {
        if (animator != null && animator.isRunning()) {
            animator.removeAllUpdateListeners();
            animator.cancel();
        }
    }

    public void postOffset(float offset) {
        offset = revisePostOffset(offset);
        relativeCurrentSelectOffset += offset;
        adjustCurrentSelectAndOffset();
        invalidate();
    }

    private float revisePostOffset(float postOffset) {
        if (postOffset > 0) {
            float useAbleOffset = currentSelectIndex * lineHeight - relativeCurrentSelectOffset;
            if (postOffset >= useAbleOffset)
                return useAbleOffset;
        }
        if (postOffset < 0) {
            float useAbleOffset = (currentSelectIndex - (adapter.getCount() - 1)) * lineHeight
                    - relativeCurrentSelectOffset;
            if (postOffset < useAbleOffset) {
                return useAbleOffset;
            }
        }
        return postOffset;
    }

    private void adjustCurrentSelectAndOffset() {
        int offsetPix = (int) relativeCurrentSelectOffset;
        int offsetRows = 0;
        // 再求出是否需要进行跨越半行的调整
        if (Math.abs(offsetPix) >= lineHeight / 2) {
            // 整数倍的调整
            offsetRows -= offsetPix / lineHeight;
            // 跨行的调整
            if (offsetPix < 0) {//上滑,数据索引增加
                ++offsetRows;
            } else {
                --offsetRows;
            }
        }
        updateViewAndCurrentSelectIndexAndOffset(offsetRows);
    }

    public void updateViewAndCurrentSelectIndexAndOffset(int offsetRows) {
        int expectIndex = currentSelectIndex + offsetRows;
        if (expectIndex < 0) {
            expectIndex = 0;
        } else if (expectIndex >= adapter.getCount()) {
            expectIndex = adapter.getCount() - 1;
        }
        int offsetIndex = expectIndex - currentSelectIndex;
        currentSelectIndex = expectIndex;
        if (onSelectedIndexChangedListener != null)
            onSelectedIndexChangedListener.onSelectedIndexChange(currentSelectIndex);
        relativeCurrentSelectOffset += lineHeight * offsetIndex;

    }

    public void setCurrentSelectIndex(int currentSelectIndex) {
        if (this.currentSelectIndex != currentSelectIndex) {
            this.currentSelectIndex = currentSelectIndex;
            if (onSelectedIndexChangedListener != null)
                onSelectedIndexChangedListener.onSelectedIndexChange(currentSelectIndex);
        }
        invalidate();
    }

    public int getCurrentSelectIndex() {
        return currentSelectIndex;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public void setOnSelectedIndexChangedListener(
            OnSelectedIndexChangedListener onSelectedIndexChangedListener) {
        this.onSelectedIndexChangedListener = onSelectedIndexChangedListener;
    }

    public interface OnSelectedIndexChangedListener {
        void onSelectedIndexChange(int index);
    }
}