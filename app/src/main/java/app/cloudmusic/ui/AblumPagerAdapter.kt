package app.cloudmusic.ui

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.support.v4.media.session.MediaSessionCompat
import androidx.viewpager.widget.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * Created by Administrator on 2018/3/30.
 */

class AblumPagerAdapter(private val mFragmentManager: FragmentManager, private val list: List<MediaSessionCompat.QueueItem>) : PagerAdapter() {
    private var mCurTransaction: FragmentTransaction? = null
    private var mCurrentPrimaryItem: Fragment? = null
    private var currentItem = 0


    override fun startUpdate(container: ViewGroup) {
        if (container.id == View.NO_ID) {
            throw IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id")
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction()
        }

        val itemId = getItemId(position)

        // Do we already have this fragment?
        val name = makeFragmentName(container.id, itemId)
        var fragment = mFragmentManager.findFragmentByTag(name)
        if (fragment != null) {
            if (DEBUG) Log.v(TAG, "Attaching item #$itemId: f=$fragment")
            mCurTransaction!!.attach(fragment)
        } else {
            fragment = getItem(position)
            if (DEBUG) Log.v(TAG, "Adding item #$itemId: f=$fragment")
            mCurTransaction!!.add(container.id, fragment,
                    makeFragmentName(container.id, itemId))
        }
        if (fragment !== mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false)
            fragment.userVisibleHint = false
        }

        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction()
        }
        if (DEBUG)
            Log.v(TAG, "Detaching item #" + getItemId(position) + ": f=" + `object`
                    + " v=" + (`object` as Fragment).view)
        var needDestory = true
        if (currentItem - 1 >= 0 && currentItem - 1 < list.size - 1) {
            val id = list[currentItem - 1].queueId
            val name = makeFragmentName(container.id, id)
            val fragment = mFragmentManager.findFragmentByTag(name)
            if (fragment === `object`) {
                needDestory = false
            }
        }

        if (currentItem - 2 >= 0 && currentItem - 2 < list.size - 1) {
            val id = list[currentItem - 2].queueId
            val name = makeFragmentName(container.id, id)
            val fragment = mFragmentManager.findFragmentByTag(name)
            if (fragment === `object`) {
                needDestory = false
            }
        }

        if (currentItem >= 0 && currentItem < list.size - 1) {
            val id = list[currentItem].queueId
            val name = makeFragmentName(container.id, id)
            val fragment = mFragmentManager.findFragmentByTag(name)
            if (fragment === `object`) {
                needDestory = false
            }
        }
        if (needDestory)
            mCurTransaction!!.detach(`object` as Fragment)

    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        if (fragment !== mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem!!.setMenuVisibility(false)
                mCurrentPrimaryItem!!.userVisibleHint = false
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true)
                fragment.userVisibleHint = true
            }
            mCurrentPrimaryItem = fragment
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        if (mCurTransaction != null) {
            mCurTransaction!!.commitNowAllowingStateLoss()
            mCurTransaction = null
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment).view === view
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    /**
     * Return a unique identifier for the item at the given position.
     *
     *
     * The default implementation returns the given position.
     * Subclasses should override this method if the positions of items can change.
     *
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    fun getItemId(position: Int): Long {
        if (position == 0)
            return -1
        return if (position == list.size + 1) -2 else list[position - 1].queueId
    }

    fun getItem(position: Int): Fragment {
        if (position == 0) {
            return AblumFragment.newInstance(list[list.size - 1].description.mediaUri!!.toString())
        }
        return if (position == list.size + 1) {
            AblumFragment.newInstance(list[0].description.mediaUri!!.toString())
        } else AblumFragment.newInstance(list[position - 1].description.mediaUri!!.toString())
    }


    override fun getCount(): Int {
        return list.size + 2
    }


    fun setCurrentItem(currentItem: Int) {
        this.currentItem = currentItem
    }

    companion object {
        private val TAG = "FragmentPagerAdapter"
        private val DEBUG = false

        private fun makeFragmentName(viewId: Int, id: Long): String {
            return "android:switcher:$viewId:$id"
        }
    }
}
