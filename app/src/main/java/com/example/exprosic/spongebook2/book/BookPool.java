package com.example.exprosic.spongebook2.book;

import android.content.Context;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.utils.Sync;

import java.util.Map;

/**
 * Created by exprosic on 4/23/2016.
 */
public abstract class BookPool {
    private Context mContext;
    private Map<String,BookItem> mBookPool;

    private Sync.CountLatch mLatch;

    public BookPool(Context context, Map<String,BookItem> bookPool) {
        mContext = context;
        mBookPool = bookPool;
    }

    final protected void loadBook(String bookId) {
        mLatch.countUp();
        MyApplication.getBookProvider().fetchBookById(mContext, bookId, new BookProvider.OnFetchedListener() {
            @Override
            public void onBookFetched(BookItem bookItem) {
                try {
                    if (bookItem == null)
                        return;
                    if (bookItem.mImageUrl == null)
                        bookItem.mImageUrl = BookItem.NO_IMAGE;
                    mBookPool.put(bookItem.mBookId, bookItem);
                } finally {
                    mLatch.countDown();
                }
            }
        });
    }

    abstract protected void iterateOverBooks();

    public void onLoaded(final Runnable runnable) {
        mLatch = new Sync.CountLatch(1);
        iterateOverBooks();
        mLatch.countDown();

        /*TODO clear thread before Activity$onDestroy*/
        new Thread() {
            @Override
            public void run() {
                Sync.awaitIgnoreInterrupt(mLatch);
                runnable.run();
            }
        }.start();
    }
}
