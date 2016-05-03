package com.example.exprosic.spongebook2.booklist;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
    protected int mUserId;

    static class BookViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.the_text_view)
        TextView mTextView;
        @Bind(R.id.the_image_view)
        ImageView mImageView;

        public BookViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        protected int getBookListPosition() {
            return getAdapterPosition();
        }
    }

    public BookListAdapter(Context context, List<BookItem> bookItems) {
        this(context, -1, bookItems);
    }

    public BookListAdapter(Context context, int userId, List<BookItem> bookItems) {
        mContext = context;
        mBookItems = bookItems;
        mUserId = userId;
    }

    protected View inflateBookItemView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType != 0)
            throw new AssertionError("viewType != 0 in BooksAdapter");
        return new BookViewHolder(inflateBookItemView(parent));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof BookViewHolder))
            throw new AssertionError("non-BookViewHolder passed to BooksAdapter");
        BookViewHolder bookViewHolder = (BookViewHolder) holder;

        String title = mBookItems.get(position).mTitle;
        if (title.length() > 8)
            title = title.substring(0, 7) + "...";
        bookViewHolder.mTextView.setText(title);
        bindImage(bookViewHolder);
        bookViewHolder.itemView.setOnClickListener(onItemClick(bookViewHolder));
    }

    protected void bindImage(BookViewHolder holder) {
        String imageUrl = mBookItems.get(holder.getBookListPosition()).mImageUrl;
        if (imageUrl == null)
            return;
        if (imageUrl.equals(BookItem.NO_IMAGE)) {
            holder.mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_image));
        } else if (imageUrl.equals(BookItem.IMAGE_LOADING)) {
            holder.mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.image_loading));
        } else {
            Picasso.with(mContext).load(imageUrl).tag(mContext).into(holder.mImageView);
        }
    }

    protected View.OnClickListener onItemClick(final BookViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookItem bookItem = mBookItems.get(holder.getBookListPosition());
                if (bookItem.isValid())
                    BookInfoActivity.startWithUser(mContext, bookItem.mBookId, mUserId);
            }
        };
    }

    @Override
    public int getItemCount() {
        return mBookItems.size();
    }
}

