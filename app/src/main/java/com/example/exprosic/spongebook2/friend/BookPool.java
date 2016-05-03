package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.util.Log;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.booklist.BookListItem;
import com.example.exprosic.spongebook2.utils.Sync;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by exprosic on 4/23/2016.
 */
public class BookPool {
    private Context mContext;
    private Map<String,BookItem> mBookPool;
    private List<UserItem> mUserItems;

    public BookPool(Context context, Map<String,BookItem> bookPool, List<UserItem> userItems) {
        mContext = context;
        mBookPool = bookPool;
        mUserItems = userItems;
    }

    public void onLoaded(final Runnable runnable) {
        int nBooks = 0;
        for (UserItem userItem: mUserItems)
            nBooks += userItem.mPreviewBookItems.size();
        final CountDownLatch bookLatch = new CountDownLatch(nBooks);

        for (UserItem userItem: mUserItems) {
            for (BookListItem bookListItems: userItem.mPreviewBookItems) {
                MyApplication.getBookProvider().fetchBookById(mContext, bookListItems.mBookId, new BookProvider.OnFetchedListener() {
                    @Override
                    public void onBookFetched(BookItem bookItem) {
                        try {
                            if (bookItem == null)
                                return;
                            if (bookItem.mImageUrl == null)
                                bookItem.mImageUrl = BookItem.NO_IMAGE;
                            mBookPool.put(bookItem.mBookId, bookItem);
                        } finally {
                            bookLatch.countDown();
                        }
                    }
                });
            }
        }

        /*TODO clear thread before Activity$onDestroy*/
        new Thread() {
            @Override
            public void run() {
                Sync.awaitIgnoreInterrupt(bookLatch);
                runnable.run();
            }
        }.start();
    }
}
