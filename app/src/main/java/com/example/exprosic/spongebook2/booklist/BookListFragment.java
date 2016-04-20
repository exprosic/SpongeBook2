package com.example.exprosic.spongebook2.booklist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.utils.Debugging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookListFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = BookListFragment.class.getSimpleName();

    @Bind(R.id.the_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.the_recycler_view)
    RecyclerView mRecyclerView;

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
        super.onStart();
        Log.d(TAG, "onStart called");
        if (mRecyclerView.getAdapter()==null // 新打开的
                || mLastRefreshTimeStamp != MyApplication.getBookListProvider().getTimeStamp()) { // 从Multiscan返回
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
            public void onBookListFetched(final List<String> bookIds) {
                {
                    // test
                    StringBuilder builder = new StringBuilder();
                    for (String bookId : bookIds) {
                        builder.append(bookId);
                        builder.append("; ");
                    }
                    Debugging.makeRawToast(getContext(), Toast.LENGTH_SHORT, builder.toString());
                }
                mLastRefreshTimeStamp = MyApplication.getBookListProvider().getTimeStamp();
                if (mBookItems != null) {
                    mBookItems.clear();
                } else {
                    mBookItems = new ArrayList<BookItem>(bookIds.size());
                }
                final CountDownLatch latch = new CountDownLatch(bookIds.size());
                for (int i = 0; i < bookIds.size(); ++i) {
                    final int idx = i;
                    MyApplication.getBookProvider().fetchBookById(getContext(), bookIds.get(i), new BookProvider.OnBookFetchedListener() {
                        @Override
                        public void onBookFetched(final BookItem bookItem) {
                            try {
                                if (bookItem == null)
                                    return;
                                if (bookItem.mImageUrl == null)
                                    bookItem.mImageUrl = BookItem.NO_IMAGE;
                                mBookItems.add(bookItem);
                            } finally {
                                latch.countDown();
                            }
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
            Log.d(TAG, String.format(Locale.US, "dataset changed, new size = %d", mRecyclerView.getAdapter().getItemCount()));
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