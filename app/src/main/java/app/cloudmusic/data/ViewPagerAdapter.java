package app.cloudmusic.data;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

/**
 * Created by Administrator on 2016/9/6.
 */
public class ViewPagerAdapter<T extends View> extends FragmentPagerAdapter {

    private List<T> arrayList;


    public ViewPagerAdapter(FragmentManager fm,List<T> arrayList) {
        super(fm);
        this.arrayList = arrayList;
    }


    @Override
    public Fragment getItem(int position) {
        return null;
    }

    // 当要显示的图片可以进行缓存的时候，会调用这个方法进行显示图片的初始化，我们将要显示的ImageView加入到ViewGroup中，然后作为返回值返回即可
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //对ViewPager页号求模取出View列表中要显示的项
        position %= arrayList.size();
        if (position<0){
            position = arrayList.size()+position;
        }
        T view = arrayList.get(position);
        //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        ViewParent vp =view.getParent();
        if (vp!=null){
            ViewGroup parent = (ViewGroup)vp;
            parent.removeView(view);
        }
        container.addView(view);
        //add listeners here if necessary
        return view;

    }

    // PagerAdapter只缓存三张要显示的图片，如果滑动的图片超出了缓存的范围，就会调用这个方法，将图片销毁
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //container.removeView(arrayList.get(position%arrayList.size()));
    }

    @Override
    public int getCount() {
        return Short.MAX_VALUE;
    }

    // 来判断显示的是否是同一张图片，这里我们将两个参数相比较返回即可
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
