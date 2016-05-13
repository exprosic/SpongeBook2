package com.example.exprosic.spongebook2.booklist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.scan.MultiscanActivity;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by exprosic on 4/16/2016.
 */
public class MyBookListAdapter extends BookListAdapter {
    private static final int VIEWTYPE_ADD = 1;

    static class MyOffsetViewHolder extends BookViewHolder {
        public MyOffsetViewHolder(View view) {
            super(view);
        }

        @Override
        protected int getBookListPosition() {
            return super.getBookListPosition()-1;
        }
    }

    static class BookAddViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.the_button)
        ImageView mImageView;
        public BookAddViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public MyBookListAdapter(Context context, Map<String,BookItem> bookPool, List<BookshelfItem> bookshelfItems) {
        super(context, bookPool, bookshelfItems);
    }

    @Override
    public int getItemViewType(int position) {
        return position==0 ?VIEWTYPE_ADD :0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new MyOffsetViewHolder(inflateBookItemView(parent));
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_add, parent, false);
        return new BookAddViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position > 0) {
            super.onBindViewHolder(holder, position - 1);
            return;
        }

        BookAddViewHolder theHolder = (BookAddViewHolder)holder;
        theHolder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiscanActivity.start(mContext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1 + super.getItemCount();
    }
}
