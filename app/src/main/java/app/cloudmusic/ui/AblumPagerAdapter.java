package app.cloudmusic.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * Created by Administrator on 2018/3/30.
 */

public class AblumPagerAdapter extends FragmentPagerAdapter{
    private List<MediaSessionCompat.QueueItem> list;
    public AblumPagerAdapter(FragmentManager fm, List<MediaSessionCompat.QueueItem> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0 ){
            return  AblumFragment.newInstance(list.get(list.size() - 1).getDescription().getMediaUri().toString());
        }
        if(position == list.size()+ 1){
            return  AblumFragment.newInstance(list.get(0).getDescription().getMediaUri().toString());
        }
        return AblumFragment.newInstance(list.get(position - 1).getDescription().getMediaUri().toString());
    }

    @Override
    public int getCount() {
        return list.size()+2;
    }
}
