package app.cloudmusic.ui;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import app.cloudmusic.R;
import app.cloudmusic.utils.broadnotify.BroadNotifyUtils;
import app.cloudmusic.utils.broadnotify.NotifyContaces;
import app.cloudmusic.utils.imageloader.ImageLoader;
import app.cloudmusic.widget.CircleImageView;

public class AblumFragment extends Fragment implements BroadNotifyUtils.MessageReceiver {
    private String mediaUri;
    private ImageLoader imageLoader;
    private CircleImageView circleImageView;
    private ObjectAnimator animator;

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
        Log.w("AAA","onCreate");
        mediaUri = getArguments().getString("mediaUri");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w("AAA","onCreateView");
        View view = inflater.inflate(R.layout.fragment_ablum, container, false);
        circleImageView = view.findViewById(R.id.imageView);
        animator = ObjectAnimator.ofFloat(circleImageView, "rotation", 0.0F, 359.0F);
        //animator = ObjectAnimator.ofFloat(getView(), "rotation", new float[]{0.0F, 360.0F});
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(25000);
        animator.setInterpolator(new LinearInterpolator());
        BroadNotifyUtils.addReceiver(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w("AAA","onActivityCreated");
        imageLoader = new ImageLoader(getContext());
        imageLoader.DisplayImage(mediaUri,circleImageView);
    }

    public void setStartAnimator(boolean flag){
        if(flag){
            if(animator.isStarted() && animator.isPaused()){
                animator.resume();
            }
            if(!animator.isStarted()){
                animator.start();
            }
        }else{
            if(animator.isStarted() && animator.isRunning()){
                animator.pause();
            }
        }
    }

    public void resetAimator(){
        animator.cancel();
    }

    @Override
    public void onMessage(int receiverType, Bundle bundle) {
        if(receiverType == NotifyContaces.UPDATE_ABLUM_ANIMATOR){
            String s = bundle.getString("mediaUri");
            if(TextUtils.equals(s,mediaUri)){
                boolean b = bundle.getBoolean("isRunning");
                if(b)
                    setStartAnimator(true);
                else
                    setStartAnimator(false);
            }else{
                resetAimator();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadNotifyUtils.removeReceiver(this);
    }
}
