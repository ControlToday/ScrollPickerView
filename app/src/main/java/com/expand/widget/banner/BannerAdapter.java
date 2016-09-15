package com.expand.widget.banner;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;

/**
 * Created by jian on 16/9/13.
 */
public class BannerAdapter extends PagerAdapter {
    Context context;
    private BitmapUtils bitmapUtils;
    private ArrayList<ImageView> imageViews;

    public BannerAdapter(Context context, ArrayList<String> urls,
                         BitmapUtils bitmapUtils) {
        this.context = context;
        this.bitmapUtils = bitmapUtils;
        initImageViews(urls);
    }

    private void initImageViews(ArrayList<String> urls) {
        if (urls == null || urls.size() == 0)
            return;
        ViewGroup.LayoutParams layoutParams  = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        imageViews = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViews.add(imageView);
            bitmapUtils.display(imageView, url);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int pos = position % imageViews.size();
        ImageView imageView = imageViews.get(pos);
        if (imageView.getParent() != null) {
            ((ViewPager) imageView
                    .getParent()).removeView(imageView);
        }
        container.addView(imageView);
        return imageView;
    }

    @Override
    public int getCount() {
        if (imageViews == null || imageViews.size() == 0) {
            return 0;
        }
        if (imageViews.size() == 1) {
            return 1;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int pos = position % imageViews.size();
        ImageView imageView = imageViews.get(pos);
        container.removeView(imageView);
    }
}
