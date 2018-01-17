package app.cloudmusic.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.cloudmusic.R;

public class AblumFragment extends Fragment {


    public AblumFragment() {
        // Required empty public constructor
    }

//    public static AblumFragment newInstance(String param1, String param2) {
//        AblumFragment fragment = new AblumFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ablum, container, false);
    }

}
