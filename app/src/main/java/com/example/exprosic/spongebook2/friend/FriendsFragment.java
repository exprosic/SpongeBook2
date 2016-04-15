package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.exprosic.spongebook2.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.the_text) TextView mTextView;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);
        mTextView.setText("friends");
        return view;
    }
}
