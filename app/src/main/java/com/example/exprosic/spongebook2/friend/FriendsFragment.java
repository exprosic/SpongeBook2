package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.booklist.BookListActivity;
import com.example.exprosic.spongebook2.utils.Debugging;
import com.example.exprosic.spongebook2.utils.Sync;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends Fragment {
    private static final String TAG = FriendsFragment.class.getSimpleName();

    @Bind(R.id.the_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.the_recycler_view) RecyclerView mRecyclerView;
    private int MENU_ITEM_ADD_FRIEND_ID;

    private List<UserItem> mUserItems;
    private Map<String,BookItem> mBookPool;

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

        setHasOptionsMenu(true);

        mUserItems = new ArrayList<>();
        mBookPool = Collections.synchronizedMap(new HashMap<String, BookItem>());
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFriends();
            }
        });
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                loadFriends();
            }
        });
        return view;
    }

    private boolean needForceRefresh = false;
    private void loadFriends() {
        FriendListProvider.OnFriendListFetchedListener listener = new FriendListProvider.OnFriendListFetchedListener() {
            @Override
            public void onFriendListFetched(final List<UserItem> userItems) {
                Log.d(TAG, String.format(Locale.US, "%d friends loaded", userItems.size()));
                mUserItems.clear();
                mUserItems.addAll(userItems);

                new BookPool(getContext(), mBookPool, mUserItems).onLoaded(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                                needForceRefresh = true;
                                renderFriends();
                            }
                        });
                    }
                });
            }
        };

        if (needForceRefresh) {
            MyApplication.getFriendListProvider().forceDownloadFriendList(getContext(), listener);
        } else {
            MyApplication.getFriendListProvider().fetchFriendList(getContext(),listener);
        }
    }

    private void renderFriends() {
        Log.d(TAG, "rendering friends' books");
        if (mRecyclerView.getAdapter() == null) {
            int columnsCount = getResources().getInteger(R.integer.item_friend_columns_per_table);
            mRecyclerView.setAdapter(new FriendAdapter(getContext(), mUserItems, mBookPool, columnsCount));
        } else {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MENU_ITEM_ADD_FRIEND_ID = menu.add(R.string.add_friend)
                .setIcon(R.drawable.ic_add_black_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                .getItemId();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ITEM_ADD_FRIEND_ID) {
            AddFriendActivity.start(getContext());
            return true;
        }

        return false;
    }
}