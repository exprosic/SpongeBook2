package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.booklist.BookListActivity;
import com.example.exprosic.spongebook2.utils.Debugging;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by exprosic on 4/22/2016.
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private static final String TAG = FriendAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<UserItem> mUserItems;
    protected Map<String,BookItem> mBookPool;
    protected int mColumnsCount;

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_user_info)
        TextView mUserInfoText;
        @Bind(R.id.table_preview_books)
        TableLayout mTableLayout;
        MyTableManager mTableManager;
        public FriendViewHolder(View view, int columnsCount) {
            super(view);
            ButterKnife.bind(this, view);
            mTableManager = new MyTableManager(mTableLayout, columnsCount);
        }
    }

    public FriendAdapter(Context context, List<UserItem> userItems, Map<String,BookItem> bookPool, int columnsCount) {
        mContext = context;
        mUserItems = userItems;
        mBookPool = bookPool;
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
    public void onBindViewHolder(final FriendViewHolder holder, int position) {
        UserItem userItem = mUserItems.get(position);
        holder.mUserInfoText.setText(userItem.nick);
        holder.mTableManager.reset();
        for (String bookId: userItem.previewBookIds) {
            BookItem bookItem = mBookPool.get(bookId);
            Debugging.myAssert(bookItem!=null, String.format(Locale.US, "bookItem is null, book pool size = %d, itemCount=%d", mBookPool.size(), getItemCount()));
            View bookView = LayoutInflater.from(mContext).inflate(R.layout.item_book, null);
            holder.mTableManager.addView(bookView);

            ImageView imageView = (ImageView)bookView.findViewById(R.id.the_image_view);
//            TextView textView = (TextView)bookView.findViewById(R.id.the_text_view);
//            textView.setText(bookItem.mTitle);
            if (bookItem.mImageUrl.equals(BookItem.NO_IMAGE)) {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_image));
            } else {
                Picasso.with(mContext).load(bookItem.mImageUrl).placeholder(R.drawable.image_loading).into(imageView);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = mUserItems.get(holder.getAdapterPosition()).userId;
                BookListActivity.startByUserId(mContext, userId);
            }
        });
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
