package com.example.exprosic.spongebook2.booklist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.scan.MultiscanActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RunnableFuture;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookListFragment extends Fragment {
    private static final String TAG = BookListFragment.class.getSimpleName();

    @Bind(R.id.the_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.the_recycler_view) RecyclerView mRecyclerView;

    private static final String PARAM_USERID = "userId";
    private int userId;

    private BookItem[] bookItems;

    public static BookListFragment newInstanceByUserId(int userId) {
        Bundle args = new Bundle();
        args.putInt(PARAM_USERID, userId);

        BookListFragment fragment = new BookListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        userId = args.getInt(PARAM_USERID);
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
        // 手动刷新
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        return view;
    }

    private void loadBookList() {
        // for test
        final int DELAY_MS = 1000;
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Map<String,String> emptyMap = new HashMap<>();
                bookItems = new BookItem[] {
                        new BookItem("book1", "book1 title", emptyMap, "http://img11.360buyimg.com/n0/18622/3c7ebf5a-7314-4308-bda5-e046968c9741.jpg"),
                        new BookItem("book2", "book2 title", emptyMap, "http://img12.360buyimg.com/n0/11116/15fa83ef-a9e6-4de5-b7a6-c04dee7dd7f5.jpg"),
                        new BookItem("book3", "book3 title", emptyMap, "http://img10.360buyimg.com/n0/16302/40ec1138-1c55-4d65-9b04-0f3ec678f47b.jpg"),
                        new BookItem("book4", "book4 title", emptyMap, "http://img10.360buyimg.com/n0/18144/684a6882-8084-435d-9a8b-f1927e91ad23.jpg")
                };
                renderBooks();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, DELAY_MS);
    }

    private void renderBooks() {
        // 刚打开的时候，由于adapter为null，即便是当前用户，也不显示[+]
        if (mRecyclerView.getAdapter() != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            if (MyApplication.isMyself(userId)) {
                mRecyclerView.setAdapter(new MyselfAdapter(getContext(), bookItems, new Runnable() {
                    @Override
                    public void run() {
                        MultiscanActivity.start(getActivity());
                    }
                }));
            } else {
                mRecyclerView.setAdapter(new BooksAdapter(getContext(), bookItems));
            }
        }
    }
}

class BooksAdapter extends RecyclerView.Adapter {
    protected Context mContext;
    protected BookItem[] mBookItems;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.the_text_view) TextView mTextView;
        @Bind(R.id.the_image_view) ImageView mImageView;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public BooksAdapter(Context context, BookItem[] bookItems) {
        mContext = context;
        mBookItems = bookItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 0)
            throw new AssertionError("viewType != 0 in BooksAdapter");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder;
        try {
            myViewHolder = (MyViewHolder)holder;
        } catch (ClassCastException e) {
            throw new AssertionError("non-MyViewHolder passed to BooksAdapter");
        }
        myViewHolder.mTextView.setText(mBookItems[position].getTitle());
        String imageUrl = mBookItems[position].getImageUrl();
        if (imageUrl != null)
            Picasso.with(mContext).load(imageUrl).tag(mContext).into(myViewHolder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mBookItems.length;
    }
}

class MyselfAdapter extends BooksAdapter {
    private static final int VIEWTYPE_ADD = 1;

    static class BookAddViewHolder extends RecyclerView.ViewHolder {
        public BookAddViewHolder(View view) {
            super(view);
        }
    }

    private Runnable mOnTriggerNewBook;

    public MyselfAdapter(Context context, BookItem[] bookItems, Runnable onTriggerNewBook) {
        super(context, bookItems);
        mOnTriggerNewBook = onTriggerNewBook;
    }

    @Override
    public int getItemViewType(int position) {
        return position==0 ?VIEWTYPE_ADD :0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return super.onCreateViewHolder(parent, viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_add, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTriggerNewBook.run();
            }
        });
        return new BookAddViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position > 0)
            super.onBindViewHolder(holder, position-1);
    }

    @Override
    public int getItemCount() {
        return 1 + super.getItemCount();
    }
}
