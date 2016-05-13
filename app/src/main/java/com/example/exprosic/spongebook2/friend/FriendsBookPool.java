package com.example.exprosic.spongebook2.friend;

import android.content.Context;

import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookPool;
import com.example.exprosic.spongebook2.booklist.BookshelfItem;

import java.util.List;
import java.util.Map;

/**
 * Created by exprosic on 5/4/2016.
 */
public class FriendsBookPool extends BookPool {
    private List<UserItem> mUserItems;

    public FriendsBookPool(Context context, Map<String,BookItem> bookPool, List<UserItem> userItems) {
        super(context, bookPool);
        mUserItems = userItems;
    }

    @Override
    public void iterateOverBooks() {
        for (UserItem userItem: mUserItems) {
            for (BookshelfItem bookshelfItems : userItem.mPreviewBookItems) {
                loadBook(bookshelfItems.getBookId());
            }
        }
    }
}
