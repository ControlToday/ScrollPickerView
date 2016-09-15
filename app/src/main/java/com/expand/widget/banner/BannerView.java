package com.expand.widget.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expand.widget.R;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;

/**
 * Created by jian on 16/9/13.
 */
public class BannerView extends FrameLayout {
    private Drawable defaultLoadingImage;
    private Drawable defaultLoadFailedImage;
    private boolean isAutoPlay;
    private Drawable indicatorDrawableSelected;
    private Drawable indicatorDrawableUnselected;
    private int indicatorWidth;
    private int indicatorHeight;
    private int indicatorMargin;
    private long animationMilliseconds;
    private long animationIntervalMilliseconds;

    BitmapUtils bitmapUtils;

    private Handler handler = new Handler();
    private boolean animationPause = false;
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (isAutoPlay && !animationPause) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                handler.postDelayed(task, animationMilliseconds + animationIntervalMilliseconds);
            }
        }
    };

    private ViewPager viewPager;
    private FrameLayout indicatorsLayout;

    private ArrayList<String> imageUrls = new ArrayList<>();
    private ImageView selectPoint;

    public BannerView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        initAttrs(context, attrs, defStyleAttr);
        initBitmapUtils();
        initChildView();
        initAnimation();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView, defStyleAttr, 0);
        defaultLoadingImage = typedArray.getDrawable(R.styleable.BannerView_default_loading_image);
        defaultLoadFailedImage = typedArray.getDrawable(R.styleable.BannerView_default_load_failed_image);
        isAutoPlay = typedArray.getBoolean(R.styleable.BannerView_auto_play, true);
        animationMilliseconds = (long) (1000 * typedArray.getFloat(R.styleable.BannerView_animation_seconds, 0));
        if (animationMilliseconds < 100) animationMilliseconds = 100;
        animationIntervalMilliseconds = (long) (1000 * typedArray.getFloat(R.styleable.BannerView_animation_interval_seconds, 0));
        if (animationIntervalMilliseconds < 100) animationIntervalMilliseconds = 100;
        indicatorWidth = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicator_width, 0);
        indicatorHeight = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicator_height, 0);
        indicatorMargin = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicator_margin, 0);
        indicatorDrawableSelected = typedArray.getDrawable(R.styleable.BannerView_indicator_drawable_selected);
        indicatorDrawableUnselected = typedArray.getDrawable(R.styleable.BannerView_indicator_drawable_unselected);
        typedArray.recycle();
    }

    private void initBitmapUtils() {
        if (isInEditMode()) {
            return;
        }
        bitmapUtils = new BitmapUtils(getContext());
        bitmapUtils.configDefaultLoadingImage(defaultLoadingImage);
        bitmapUtils.configDefaultLoadFailedImage(defaultLoadFailedImage);
    }

    private void initChildView() {
        viewPager = getViewPager();
        addView(viewPager);
        setBannerAdapter();
    }

    private void initAnimation() {
        setAutoPlay(isAutoPlay);
    }

    @NonNull
    private ViewPager getViewPager() {
        ViewPager viewPager = new ViewPager(getContext());
        LayoutParams viewPagerParams =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(viewPagerParams);
        ViewPagerScroller scroller = new ViewPagerScroller(getContext());
        scroller.setScrollDuration((int) (animationMilliseconds));
        scroller.initViewPagerScroll(viewPager);
        return viewPager;
    }

    @NonNull
    private FrameLayout getIndicators() {
        // 最外层的父容器
        FrameLayout parent = new FrameLayout(getContext());
        FrameLayout.LayoutParams parentParams =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        parentParams.bottomMargin = indicatorMargin;
        parentParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        parent.setLayoutParams(parentParams);

        // 默认点点的集合的容器是indicators
        LinearLayout indicators = new LinearLayout(getContext());
        FrameLayout.LayoutParams indicatorsParams =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicators.setLayoutParams(indicatorsParams);
        // LinearLayout_gravity
        int indicatorSize = imageUrls.size();
        if (isInEditMode()) {
            indicatorSize = 6;
        }

        // 默认点点,动态生成点点包括默认点点和选择样式点点
        for (int i = 0; i < indicatorSize; i++) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(indicatorWidth, indicatorHeight);
            layoutParams.leftMargin = indicatorMargin / 2;
            layoutParams.rightMargin = indicatorMargin / 2;
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageDrawable(indicatorDrawableUnselected);
            indicators.addView(imageView);
        }

        // 选中点点,
        selectPoint = new ImageView(getContext());
        FrameLayout.LayoutParams selectParams =
                new LayoutParams(indicatorWidth, indicatorHeight);
        selectParams.leftMargin = indicatorMargin / 2;
        selectParams.rightMargin = indicatorMargin / 2;
        selectPoint.setLayoutParams(selectParams);
        selectPoint.setScaleType(ImageView.ScaleType.CENTER_CROP);
        selectPoint.setImageDrawable(indicatorDrawableSelected);

        // 将选中点和默认点放入,放入FragmentLayout中
        parent.addView(indicators);
        parent.addView(selectPoint);

        return parent;

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animationPause = true;
                if (isAutoPlay) {
                    stopAutoPlay();
                }
                break;
            case MotionEvent.ACTION_UP:
                animationPause = false;
                if (isAutoPlay) {
                    startAutoPlay();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        if (isAutoPlay) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }
    }

    private void startAutoPlay() {
        handler.removeCallbacks(task);
        handler.postDelayed(task, animationIntervalMilliseconds);
    }

    private void stopAutoPlay() {
        handler.removeCallbacks(task);
    }

    private void setBannerAdapter() {
        viewPager.setAdapter(new BannerAdapter(getContext(), imageUrls, bitmapUtils));
        removeView(indicatorsLayout);
        indicatorsLayout = getIndicators();
        addView(indicatorsLayout);
        setIndicatorMoves();
    }

    private void setIndicatorMoves() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float realPosition = position % imageUrls.size() + positionOffset;
                float trans = (indicatorWidth + indicatorMargin) * realPosition;
                int maxTrans = indicatorsLayout.getWidth() - (indicatorMargin + indicatorWidth) / 2;
                if (trans > maxTrans) {
                    trans = trans - indicatorsLayout.getWidth();
                }
                selectPoint.setTranslationX(trans);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void setIndicatorsVisibility(boolean visibility) {
        if(visibility) {
            indicatorsLayout.setVisibility(View.VISIBLE);
        } else {
            indicatorsLayout.setVisibility(View.GONE);
        }
    }
    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
        setBannerAdapter();
    }

    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }
}
