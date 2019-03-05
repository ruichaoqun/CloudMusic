package app.cloudmusic.ui;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.viewpager.widget.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 2018/3/30.
 */

public class AblumPagerAdapter extends PagerAdapter {
    private static final String TAG = "FragmentPagerAdapter";
    private static final boolean DEBUG = false;

    private List<MediaSessionCompat.QueueItem> list;
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentPrimaryItem = null;
    private int currentItem = 0;

    public AblumPagerAdapter(FragmentManager fm,List<MediaSessionCompat.QueueItem> list1) {
        mFragmentManager = fm;
        list = list1;
    }


    @Override
    public void startUpdate(ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id");
        }
    }

    @SuppressWarnings("ReferenceEquality")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        final long itemId = getItemId(position);

        // Do we already have this fragment?
        String name = makeFragmentName(container.getId(), itemId);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            if (DEBUG) Log.v(TAG, "Attaching item #" + itemId + ": f=" + fragment);
            mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            if (DEBUG) Log.v(TAG, "Adding item #" + itemId + ": f=" + fragment);
            mCurTransaction.add(container.getId(), fragment,
                    makeFragmentName(container.getId(), itemId));
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Detaching item #" + getItemId(position) + ": f=" + object
                + " v=" + ((Fragment)object).getView());
        boolean needDestory = true;
        if(currentItem - 1 >= 0 && currentItem - 1 < list.size() - 1){
            long id = list.get(currentItem - 1).getQueueId();
            String name = makeFragmentName(container.getId(), id);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if(fragment == object){
                needDestory = false;
            }
        }

        if(currentItem - 2 >= 0 && currentItem - 2 < list.size() - 1 ){
            long id = list.get(currentItem - 2).getQueueId();
            String name = makeFragmentName(container.getId(), id);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if(fragment == object){
                needDestory = false;
            }
        }

        if(currentItem >= 0 && currentItem < list.size() - 1 ){
            long id = list.get(currentItem).getQueueId();
            String name = makeFragmentName(container.getId(), id);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if(fragment == object){
                needDestory = false;
            }
        }
        if(needDestory)
            mCurTransaction.detach((Fragment) object);

    }

    @SuppressWarnings("ReferenceEquality")
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitNowAllowingStateLoss();
            mCurTransaction = null;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    /**
     * Return a unique identifier for the item at the given position.
     *
     * <p>The default implementation returns the given position.
     * Subclasses should override this method if the positions of items can change.</p>
     *
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    public long getItemId(int position) {
        if(position == 0)
            return -1;
        if(position == list.size()+1)
            return -2;
        return list.get(position - 1).getQueueId();
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

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


    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }
}
