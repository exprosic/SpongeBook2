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

        int columnsCount = getResources().getInteger(R.integer.item_friend_columns_per_table);
        mUserItems = new ArrayList<>();
        mBookPool = Collections.synchronizedMap(new HashMap<String, BookItem>());
        mRecyclerView.setAdapter(new FriendAdapter(getContext(), mUserItems, mBookPool, columnsCount));
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

    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadFriends() {
        MyApplication.getFriendListProvider().fetchFriendList(getContext(), new FriendListProvider.OnFriendListFetchedListener() {
            @Override
            public void onFriendListFetched(final List<UserItem> userItems) {
                Log.d(TAG, String.format(Locale.US, "%d friends loaded", userItems.size()));
                mUserItems.clear();
                mUserItems.addAll(userItems);

                int nBooks = 0;
                for (UserItem userItem: userItems)
                    nBooks += userItem.previewBookIds.size();
                Log.d(TAG, String.format(Locale.US, "nbooks = %d", nBooks));
                final CountDownLatch bookLatch = new CountDownLatch(nBooks);

                for (UserItem userItem: userItems) {
                    for (String bookId: userItem.previewBookIds) {
                        MyApplication.getBookProvider().fetchBookById(getContext(), bookId, new BookProvider.OnFetchedListener() {
                            @Override
                            public void onBookFetched(BookItem bookItem) {
                                try {
                                    if (bookItem == null)
                                        return;
                                    if (bookItem.mImageUrl == null)
                                        bookItem.mImageUrl = BookItem.NO_IMAGE;
                                    mBookPool.put(bookItem.mBookId, bookItem);
                                    Log.d(TAG, String.format(Locale.US, "book pool size: %d", mBookPool.size()));
                                } finally {
                                    bookLatch.countDown();
                                }
                            }
                        });
                    }
                }

                new Thread() {
                    @Override
                    public void run() {
                        Sync.awaitIgnoreInterrupt(bookLatch);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            /**/
                        }
                        mSwipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                                renderFriends();
                            }
                        });

                    }
                }.start();
            }
        });
    }

    private void renderFriends() {
        Log.d(TAG, "rendering friends' books");
        mRecyclerView.getAdapter().notifyDataSetChanged();
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