package com.example.exprosic.spongebook2.booklist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookInfoActivity;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.scan.MultiscanActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookListFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = BookListFragment.class.getSimpleName();

    @Bind(R.id.the_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.the_recycler_view) RecyclerView mRecyclerView;

    private static final String PARAM_USER_ID = "userId";
    private int mUserId;

    private long mLastRefreshTimeStamp;
    private List<BookItem> mBookItems;

    public static BookListFragment newInstanceByUserId(int userId) {
        Bundle args = new Bundle();
        args.putInt(PARAM_USER_ID, userId);

        BookListFragment fragment = new BookListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUserId = args.getInt(PARAM_USER_ID);
        mLastRefreshTimeStamp = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        ButterKnife.bind(this, view);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBookList();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        if (mLastRefreshTimeStamp != MyApplication.getBookListProvider().getTimeStamp()) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    loadBookList();
                }
            });
        }
    }

    private void loadBookList() {
        MyApplication.getBookListProvider().fetchBookList(getContext(), mUserId, new BookListProvider.OnBookListFetchedListener() {
            @Override
                public void onBookListFetched(List<String> bookIds) {
                {
                    // test
                    StringBuilder builder = new StringBuilder();
                    for (String bookId: bookIds) {
                        builder.append(bookId);
                        builder.append("; ");
                    }
                    Toast.makeText(getContext(), builder.toString(), Toast.LENGTH_SHORT).show();
                }
                mLastRefreshTimeStamp = MyApplication.getBookListProvider().getTimeStamp();
                mBookItems = new ArrayList<BookItem>(bookIds.size());
                final CountDownLatch latch = new CountDownLatch(bookIds.size());
                for (int i=0; i<bookIds.size(); ++i) {
                    final int idx = i;
                    MyApplication.getBookProvider().fetchBookById(getContext(), bookIds.get(i), new BookProvider.OnBookFetchedListener() {
                        @Override
                        public void onBookFetched(final BookItem bookItem) {
                            mBookItems.add(bookItem);
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(),
                                            String.format(Locale.CHINESE, "title:%s,image:%s", bookItem.getTitle(), bookItem.getImageUrl()),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            latch.countDown();
                        }
                    });
                }

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            latch.await();
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    renderBooks();
                                }
                            });
                        } catch (InterruptedException e) {
                            Log.d(TAG, "CountDownLatch interrupted", e);
                        }
                    }
                }.start();
            }
        });
    }

    private void renderBooks() {
        assert mBookItems != null;
        // 刚打开时，由于adapter为null，即便是当前用户，也不显示[+]
        if (mRecyclerView.getAdapter() != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            if (MyApplication.isMyself(mUserId)) {
                mRecyclerView.setAdapter(new MyBookListAdapter(getContext(), mBookItems));
            } else {
                mRecyclerView.setAdapter(new BookListAdapter(getContext(), mBookItems));
            }
        }
    }
}