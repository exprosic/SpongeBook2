package com.example.exprosic.spongebook2.surroundings;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.exprosic.spongebook2.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SurroundingsFragment extends Fragment {
    private static final String TAG = SurroundingsFragment.class.getSimpleName();

    @Bind(R.id.the_text) TextView mTextView;

    public static SurroundingsFragment newInstance() {
        return new SurroundingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_surroundings, container, false);
        ButterKnife.bind(this, view);
        mTextView.setText("surrounding");
        return view;
    }
}
