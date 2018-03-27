package app.cloudmusic.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.cloudmusic.R;
import app.cloudmusic.utils.imageloader.ImageLoader;
import app.cloudmusic.widget.CircleImageView;

public class AblumFragment extends Fragment {
    private String mediaUri;
    private ImageLoader imageLoader;
    private CircleImageView circleImageView;

    public AblumFragment() {
        // Required empty public constructor
    }

    public static AblumFragment newInstance(String mediaUri) {
        AblumFragment fragment = new AblumFragment();
        Bundle args = new Bundle();
        args.putString("mediaUri", mediaUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaUri = getArguments().getString("mediaUri");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ablum, container, false);
        circleImageView = view.findViewById(R.id.imageView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imageLoader = new ImageLoader(getContext());
        imageLoader.DisplayImage(mediaUri,circleImageView);
    }
}
