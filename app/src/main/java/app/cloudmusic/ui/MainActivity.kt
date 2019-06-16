package app.cloudmusic.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View

import java.util.ArrayList

import app.cloudmusic.BroswerAdapter
import app.cloudmusic.Contaces
import app.cloudmusic.R
import app.cloudmusic.data.MediaDataInfo
import app.cloudmusic.utils.MediaUtils

class MainActivity : BaseMediaActivity(), NavigationView.OnNavigationItemSelectedListener, BroswerAdapter.OnItemClickListener {
    private var recyclerView: RecyclerView? = null
    private val isResumePermission = false//是否在执行权限申请
    private var broswerAdapter: BroswerAdapter? = null
    private val arraylist = ArrayList<MediaBrowserCompat.MediaItem>()
    private val fragment: Fragment? = null

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            if (children != null) {
                arraylist.clear()
                arraylist.addAll(children)
                broswerAdapter!!.notifyDataSetChanged()
            }
        }

        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>, options: Bundle) {
            super.onChildrenLoaded(parentId, children, options)
        }

        override fun onError(parentId: String) {
            super.onError(parentId)
        }

        override fun onError(parentId: String, options: Bundle) {
            super.onError(parentId, options)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        broswerAdapter = BroswerAdapter(this, arraylist)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = broswerAdapter
        broswerAdapter!!.setOnItemClickListener(this)


        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)


    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onMediaControllerConnected() {
        super.onMediaControllerConnected()
        mMediaBrowser.unsubscribe(Contaces.SERVICE_ID_LOCALMUSIC)
        mMediaBrowser.subscribe(Contaces.SERVICE_ID_LOCALMUSIC, subscriptionCallback)
        MediaControllerCompat.getMediaController(this).registerCallback(mMediaControllerCallback)
    }


    override fun onItemClick(holder: RecyclerView.ViewHolder, position: Int) {
        PlayFromMediaId(position)
    }

    private fun PlayFromMediaId(position: Int) {
        val mediaId = arraylist[position].mediaId
        val bundle = Bundle()
        bundle.putString("title", "本地歌曲")
        val list = MediaUtils.tarsformToMediaDataInfo(arraylist)
        bundle.putParcelableArrayList("list", list as ArrayList<out Parcelable>)
        MediaControllerCompat.getMediaController(this).transportControls.playFromMediaId(mediaId, bundle)
    }
}
