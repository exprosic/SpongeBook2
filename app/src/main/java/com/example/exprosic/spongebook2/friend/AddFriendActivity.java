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
import com.example.exprosic.spongebook2.utils.Debugging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int mColumnsCount;

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

        mUserItems = new ArrayList<>();
        mBookPool = Collections.synchronizedMap(new HashMap<String, BookItem>());
        mColumnsCount = getResources().getInteger(R.integer.item_search_friend_columns_per_table);

        loadRequests();
    }

    private boolean requestsLoaded = false; // preventing bugs, not sure
    private void loadRequests() {
        if (requestsLoaded)
            return;
        requestsLoaded = true;
        mSearchButton.setText(R.string.loading);
        mSearchButton.setClickable(false);
        MyApplication.getFriendListProvider().fetchFriendRequests(this, new FriendListProvider.OnFriendRequestsFetchedListener() {
            @Override
            public void onFriendRequestsFetched(List<UserItem> userItems) {
                if (userItems == null) {
                    Toast.makeText(AddFriendActivity.this, R.string.load_failed, Toast.LENGTH_SHORT).show();
                    mSearchButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mSearchButton.setText(R.string.search_friends);
                            mSearchButton.setClickable(true);
                        }
                    });
                    return;
                }
                mUserItems.clear();
                mUserItems.addAll(userItems);
                new FriendsBookPool(AddFriendActivity.this, mBookPool, mUserItems).onLoaded(new Runnable() {
                    @Override
                    public void run() {
                        mSearchButton.post(new Runnable() {
                            @Override
                            public void run() {
                                mSearchButton.setText(R.string.search_friends);
                                mSearchButton.setClickable(true);
                                renderRequests();
                            }
                        });
                    }
                });
            }
        });
    }

    private void renderRequests() {
        mRecyclerView.setAdapter(new SearchFriendAdapter(this, mUserItems, mBookPool, mColumnsCount) {
            @Override
            protected void loadButton(final SearchFriendViewHolder holder, int position) {
                holder.mRequestButton.setText(R.string.accept_request);
                holder.mRequestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.mRequestButton.setText(R.string.loading);
                        holder.mRequestButton.setClickable(false);
                        UserItem userItem = mUserItems.get(holder.getAdapterPosition());
                        MyApplication.getFriendListProvider().newFriendAccept(AddFriendActivity.this, userItem.mUserId, new FriendListProvider.OnFriendAcceptedListener() {
                            @Override
                            public void onFriendAccepted(boolean isSucceeded) {
                                if (isSucceeded) {
                                    holder.mRequestButton.setText(R.string.request_accepted);
                                } else {
                                    Toast.makeText(AddFriendActivity.this, R.string.load_failed, Toast.LENGTH_SHORT).show();
                                    holder.mRequestButton.setText(R.string.accept_request);
                                    holder.mRequestButton.setClickable(true);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private boolean didSearch = false;
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
                if (userItems == null) {
                    Toast.makeText(AddFriendActivity.this, R.string.search_friends_failed, Toast.LENGTH_SHORT).show();
                    mSearchButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mSearchButton.setText(R.string.search_friends);
                            mSearchButton.setClickable(true);
                        }
                    });
                    return;
                }
                Debugging.makeToast(AddFriendActivity.this, Toast.LENGTH_SHORT, "%d", userItems.size());
                mUserItems.clear();
                mUserItems.addAll(userItems);
                new FriendsBookPool(AddFriendActivity.this, mBookPool, mUserItems).onLoaded(new Runnable() {
                    @Override
                    public void run() {
                        mSearchButton.post(new Runnable() {
                            @Override
                            public void run() {
                                mSearchButton.setText(R.string.search_friends);
                                mSearchButton.setClickable(true);
                                renderResult();
                            }
                        });
                    }
                });
            }
        });
    }

    private void renderResult() {
        Log.d(TAG, "rendering search result");
        if (didSearch) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            didSearch = true;
            mRecyclerView.setAdapter(new SearchFriendAdapter(this, mUserItems, mBookPool, mColumnsCount));
        }
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
//        @Bind(R.id.button_request)
        Button mRequestButton;
        public SearchFriendViewHolder(View view, int columnsCount) {
            super(view, columnsCount);
            mRequestButton = (Button)view.findViewById(R.id.button_request);
//            ButterKnife.bind(this, view);
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
        SearchFriendViewHolder theHolder = (SearchFriendViewHolder)holder;
        loadButton(theHolder, position);
    }

    protected void loadButton(final SearchFriendViewHolder holder, int position) {
        holder.mRequestButton.setText(R.string.add_friend);
        holder.mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = mUserItems.get(holder.getAdapterPosition()).mUserId;
                MyApplication.getFriendListProvider().newFriendRequest(mContext, userId, new FriendListProvider.OnFriendRequestedListener() {
                    @Override
                    public void onFriendRequested(boolean isSucceeded) {
                        if (isSucceeded) {
                            holder.mRequestButton.setClickable(false);
                            holder.mRequestButton.setText(R.string.friend_requested_msg);
                        } else {
                            Debugging.makeRawToast(mContext, Toast.LENGTH_SHORT, mContext.getString(R.string.friend_request_failed_msg));
                        }
                    }
                });
            }
        });
    }
}