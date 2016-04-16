package com.example.exprosic.spongebook2.booklist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookInfoActivity;
import com.example.exprosic.spongebook2.book.BookItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by exprosic on 4/16/2016.
 */
public class BookListAdapter extends RecyclerView.Adapter {
    protected Context mContext;
    protected List<BookItem> mBookItems;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.the_text_view)
        TextView mTextView;
        @Bind(R.id.the_image_view)
        ImageView mImageView;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public BookListAdapter(Context context, List<BookItem> bookItems) {
        mContext = context;
        mBookItems = bookItems;
    }

    protected View inflateBookItemView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 0)
            throw new AssertionError("viewType != 0 in BooksAdapter");
        return new MyViewHolder(inflateBookItemView(parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof MyViewHolder))
            throw new AssertionError("non-MyViewHolder passed to BooksAdapter");
        MyViewHolder myViewHolder = (MyViewHolder)holder;

        String title = mBookItems.get(position).getTitle();
        if (title.length() > 10)
            title = title.substring(0,7) + "...";
        myViewHolder.mTextView.setText(title);
        String imageUrl = mBookItems.get(position).getImageUrl();
        if (imageUrl != null)
            Picasso.with(mContext).load(imageUrl).tag(mContext).into(myViewHolder.mImageView);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookInfoActivity.startWithBookId(mContext, mBookItems.get(holder.getAdapterPosition()).getBookId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBookItems.size();
    }
}

