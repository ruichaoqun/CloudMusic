package app.cloudmusic.ui;

import android.content.Context;

import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import app.cloudmusic.R;
import app.cloudmusic.utils.imageloader.ImageLoader;
import app.cloudmusic.widget.CircleImageView;

/**
 * Created by Administrator on 2018/1/22.
 */

public class UltraPagerAdapter extends PagerAdapter {
    private List<String> mediaUris;
    private ImageLoader imageLoader;

    public UltraPagerAdapter(Context context,List<String> mediaUris) {
        this.mediaUris = mediaUris;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
        return mediaUris.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_ablum, null);
        CircleImageView circleImageView = relativeLayout.findViewById(R.id.imageView);
        container.addView(relativeLayout);
        imageLoader.DisplayImage(mediaUris.get(position),circleImageView);
        return relativeLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}