package com.krg.gamestore.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.krg.gamestore.R;

import java.util.List;

public class ScreenshotPagerAdapter extends PagerAdapter {

    private List<String> screenshotUrls;
    private LayoutInflater inflater;

    public ScreenshotPagerAdapter(Context context, List<String> screenshotUrls) {
        this.screenshotUrls = screenshotUrls;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return screenshotUrls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.item_screenshot, container, false);

        ImageView imageView = view.findViewById(R.id.imageViewScreenshot);
        Glide.with(container.getContext())
                .load(screenshotUrls.get(position))
                .into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
