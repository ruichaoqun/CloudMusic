package app.cloudmusic.ui;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import app.cloudmusic.BroswerAdapter;
import app.cloudmusic.Contaces;
import app.cloudmusic.R;
import app.cloudmusic.data.MediaDataInfo;
import app.cloudmusic.utils.MediaUtils;

public class MainActivity extends BaseMediaActivity
        implements NavigationView.OnNavigationItemSelectedListener, BroswerAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private boolean isResumePermission = false;//是否在执行权限申请
    private BroswerAdapter broswerAdapter;
    private ArrayList<MediaBrowserCompat.MediaItem> arraylist = new ArrayList<>();
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        broswerAdapter = new BroswerAdapter(this,arraylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(broswerAdapter);
        broswerAdapter.setOnItemClickListener(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onMediaControllerConnected() {
        super.onMediaControllerConnected();
            mMediaBrowser.unsubscribe(Contaces.SERVICE_ID_LOCALMUSIC);
            mMediaBrowser.subscribe(Contaces.SERVICE_ID_LOCALMUSIC,subscriptionCallback);
            MediaControllerCompat.getMediaController(this).registerCallback(mMediaControllerCallback);
    }

    private MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            if(children != null){
                arraylist.clear();
                arraylist.addAll(children);
                broswerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
            super.onChildrenLoaded(parentId, children, options);
        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
        }

        @Override
        public void onError(@NonNull String parentId, @NonNull Bundle options) {
            super.onError(parentId, options);
        }
    };




    @Override
    public void onItemClick(RecyclerView.ViewHolder holder, int position) {
        PlayFromMediaId(position);
    }

    private void PlayFromMediaId(int position){
        String mediaId = arraylist.get(position).getMediaId();
        Bundle bundle = new Bundle();
        bundle.putString("title","本地歌曲");
        List<MediaDataInfo> list = MediaUtils.tarsformToMediaDataInfo(arraylist);
        bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) list);
        MediaControllerCompat.getMediaController(this).getTransportControls().playFromMediaId(mediaId,bundle);
    }
}
