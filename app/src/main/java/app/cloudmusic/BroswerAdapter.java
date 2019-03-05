package app.cloudmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.media.MediaBrowserCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.cloudmusic.utils.imageloader.ImageLoader;

/**
 * Created by Administrator on 2017/11/13.
 */

public class BroswerAdapter extends RecyclerView.Adapter<BroswerAdapter.ViewHolder> {
    private List<MediaBrowserCompat.MediaItem> arrayList;
    private Context context;
    private ImageLoader imageLoader;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public BroswerAdapter(Context context, List<MediaBrowserCompat.MediaItem> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adapter_broswer,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        MediaBrowserCompat.MediaItem item = arrayList.get(position);

        holder.title.setText(item.getDescription().getTitle());
        holder.description.setText(item.getDescription().getSubtitle()+" - "+item.getDescription().getDescription());
        imageLoader.DisplayImage(item.getDescription().getMediaUri().toString(),holder.cover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(holder,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public Bitmap getBitmap(String url) {
        long time1 = System.currentTimeMillis();
        try {
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(url);
            Bitmap bitmap = null;

            byte[] data = mmr.getEmbeddedPicture();

            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                long time2 = System.currentTimeMillis();
                return bitmap;
            } else {
                return null;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView cover;
        public TextView title;
        public TextView description;
        public ViewHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(RecyclerView.ViewHolder holder,int position);
    }
}
