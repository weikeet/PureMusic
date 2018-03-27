package io.weicools.puremusic.module;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.weicools.puremusic.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MySheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MySheetFragment extends Fragment {

    public MySheetFragment() {
        // Required empty public constructor
    }

    public static MySheetFragment newInstance() {
        return new MySheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_sheet, container, false);
    }
}
