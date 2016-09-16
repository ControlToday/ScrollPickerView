package com.expand.widget.banner;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
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
    private ArrayList<String> urls;

    public BannerAdapter(Context context, ArrayList<String> urls,
                         BitmapUtils bitmapUtils) {
        this.context = context;
        this.bitmapUtils = bitmapUtils;
        this.urls = urls;
    }

    private ImageView getImageView(String url) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bitmapUtils.display(imageView, url);
        return imageView;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int pos = position % urls.size();
        ImageView imageView = getImageView(urls.get(pos));
        container.addView(imageView);
        return imageView;
    }

    @Override
    public int getCount() {
        if (urls == null || urls.size() == 0) {
            return 0;
        }
        if (urls.size() == 1) {
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
        container.removeView((View) object);
    }
}
