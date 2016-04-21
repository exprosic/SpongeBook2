package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.utils.Debugging;
import com.example.exprosic.spongebook2.utils.Sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddFriendActivity extends AppCompatActivity {
    public static final String TAG = AddFriendActivity.class.getSimpleName();

    @Bind(R.id.the_toolbar) Toolbar mToolBar;
    @Bind(R.id.edit_search) EditText mSearchEdit;
    @Bind(R.id.button_search) Button mSearchButton;
    @Bind(R.id.the_recycler_view) RecyclerView mRecyclerView;

    private List<UserItem> mUserItems;
    private Map<String,BookItem> mBookPool;

    public static void start(Context context) {
        Intent intent = new Intent(context, AddFriendActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int columnsCount = getResources().getInteger(R.integer.item_search_friend_columns_per_table);
        mUserItems = new ArrayList<>();
        mBookPool = Collections.synchronizedMap(new HashMap<String, BookItem>());
        mRecyclerView.setAdapter(new SearchFriendAdapter(this, mUserItems, mBookPool, columnsCount));
    }

    @OnClick(R.id.button_search)
    void doSearch() {
        String pattern = mSearchEdit.getText().toString().trim();
        if (pattern.length() == 0) {
            Toast.makeText(this, R.string.search_pattern_empty_msg, Toast.LENGTH_SHORT).show();
            return;
        }
        mSearchButton.setText(R.string.searching_friends);
        mSearchButton.setClickable(false);
        MyApplication.getFriendSearcher().fetchUsers(this, pattern, new FriendSearcher.OnFriendSearchedListener() {
            @Override
            public void onFriendSearched(List<UserItem> userItems) {
                Debugging.makeToast(AddFriendActivity.this, Toast.LENGTH_SHORT, "%d", userItems.size());

                /* remove myself */
                mUserItems.clear();
                for (UserItem userItem: userItems)
                    if (!MyApplication.isMyself(userItem.userId))
                        mUserItems.add(userItem);

                int nBooks = 0;
                for (UserItem userItem: mUserItems)
                    nBooks += userItem.previewBookIds.size();
                final CountDownLatch bookLatch = new CountDownLatch(nBooks);

                for (UserItem userItem: mUserItems) {
                    for (String bookId: userItem.previewBookIds) {
                        MyApplication.getBookProvider().fetchBookById(AddFriendActivity.this, bookId, new BookProvider.OnFetchedListener() {
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
                        mSearchButton.post(new Runnable() {
                            @Override
                            public void run() {
                                mSearchButton.setText(R.string.search_friends);
                                mSearchButton.setClickable(true);
                                mRecyclerView.getAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                }.start();

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

class SearchFriendAdapter extends FriendAdapter {
    public SearchFriendAdapter(Context context, List<UserItem> userItems, Map<String,BookItem> bookPool, int columnsCount) {
        super(context, userItems, bookPool, columnsCount);
    }

    static class SearchFriendViewHolder extends FriendAdapter.FriendViewHolder {
        @Bind(R.id.button_request) Button mRequestButton;
        public SearchFriendViewHolder(View view, int columnsCount) {
            super(view, columnsCount);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public SearchFriendViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_friend, parent, false);
        return new SearchFriendViewHolder(view, mColumnsCount);
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final SearchFriendViewHolder theHolder = (SearchFriendViewHolder)holder;
        theHolder.mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = mUserItems.get(theHolder.getAdapterPosition()).userId;
                MyApplication.getFriendListProvider().newFriendRequest(mContext, userId, new FriendListProvider.OnFriendRequestedListener() {
                    @Override
                    public void onFriendRequested(boolean isSucceeded) {
                        if (isSucceeded) {
                            theHolder.mRequestButton.setClickable(false);
                            theHolder.mRequestButton.setText(R.string.friend_requested_msg);
                        } else {
                            Debugging.makeRawToast(mContext, Toast.LENGTH_SHORT, mContext.getString(R.string.friend_request_failed_msg));
                        }
                    }
                });
            }
        });
    }
}