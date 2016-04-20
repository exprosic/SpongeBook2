package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.utils.Debugging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.the_recycler_view) RecyclerView mRecyclerView;

    private List<UserItem> mUserItems;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);
        mUserItems = new ArrayList<>();
        for (int i=0; i<11; ++i)
            mUserItems.add(null);
        int columnsCount = getResources().getInteger(R.integer.item_friend_columns_per_table);
        mRecyclerView.setAdapter(new FriendAdapter(getContext(), mUserItems, columnsCount));
        return view;
    }
}

class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private static final String TAG = FriendAdapter.class.getSimpleName();

    private Context mContext;
    private List<UserItem> mUserItems;
    private int mColumnsCount;

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.user_info_text) TextView mUserInfoText;
        @Bind(R.id.preview_books_table) TableLayout mTableLayout;
        MyTableManager mTableManager;
        public FriendViewHolder(View view, int columnsCount) {
            super(view);
            ButterKnife.bind(this, view);
            mTableManager = new MyTableManager(mTableLayout, columnsCount);
        }
    }

    public FriendAdapter(Context context, List<UserItem> userItems, int columnsCount) {
        mContext = context;
        mUserItems = userItems;
        mColumnsCount = columnsCount;
    }

    @Override
    public int getItemCount() {
        return mUserItems.size();
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view, mColumnsCount);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.mTableManager.reset();
        holder.mUserInfoText.setText(String.format(Locale.US, "position=%1$d%1$d", position));
        int[] colors = new int[] {Color.GREEN, Color.YELLOW, Color.BLACK, Color.RED, Color.CYAN, Color.BLUE, Color.MAGENTA};
        for (int i=0; i<(position%4==1||position%4==2?0:7); ++i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_book, null);
            ImageView imageView = (ImageView)view.findViewById(R.id.the_image_view);
            TextView textView = (TextView)view.findViewById(R.id.the_text_view);
            imageView.setBackgroundColor(colors[i]);
            textView.setText(Integer.toString((i)));

            holder.mTableManager.addView(view);
            Debugging.setLayoutParam(view, "rightMargin", mContext.getResources().getDimensionPixelSize(R.dimen.item_book_margin_horizontal));
        }
    }
}

class MyTableManager {
    private Context mContext;
    private TableLayout mTableLayout;
    private final int mColumnsCount;
    private int mNextPosition;
    private TableRow mLastTableRow;

    public MyTableManager(TableLayout tableLayout, int columnsCount) {
        Debugging.myAssert(tableLayout.getChildCount()==0, "passing in non-empty TableLayout");
        mTableLayout = tableLayout;
        mContext = tableLayout.getContext();
        mColumnsCount = columnsCount;
        mNextPosition = 0;
        mLastTableRow = null;
    }

    public void reset() {
        mTableLayout.removeAllViews();
        mNextPosition = 0;
        mLastTableRow = null;
    }

    public void addView(View view) {
        if (mNextPosition == 0) {
            mLastTableRow = new TableRow(mContext);
            for (int i=0; i<mColumnsCount; ++i) {
                Space space = new Space(mContext);
                mLastTableRow.addView(space);
                setFairWeight(space);
            }
            mTableLayout.addView(mLastTableRow);
        }
        mLastTableRow.removeViewAt(mNextPosition);
        mLastTableRow.addView(view, mNextPosition);
        setFairWeight(view);
        mNextPosition = (mNextPosition+1) % mColumnsCount;
    }

    private void setFairWeight(View view) {
        TableRow tableRow = (TableRow)view.getParent();
        TableRow.LayoutParams params = (TableRow.LayoutParams)view.getLayoutParams();
        params.weight = 1.0f;
        view.setLayoutParams(params);
    }
}