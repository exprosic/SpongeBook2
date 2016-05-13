package com.example.exprosic.spongebook2.booklist;

import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by exprosic on 5/3/2016.
 */
public class BookshelfItem implements Serializable {
    public static final String TAG = BookshelfItem.class.getSimpleName();

    protected int mUserId;
    protected String mBookId;

    public BookshelfItem(int userId, String bookId) {
        mUserId = userId;
        mBookId = bookId;
    }

    public int getUserId() {
        return mUserId;
    }

    public String getBookId() {
        return mBookId;
    }

    protected static final String FIELD_USER_ID = "userId";
    protected static final String FIELD_BOOK_ID = "bookId";

    public BookshelfItem(JSONObject jsonObject) throws JSONException {
        mUserId = jsonObject.getInt(FIELD_USER_ID);
        mBookId = jsonObject.getString(FIELD_BOOK_ID);
    }

    public void insertIntoDb(SQLiteDatabase db) {
        throw new UnsupportedOperationException();
    }

    public static List<BookshelfItem> getCollectionFromJson(JSONObject jsonObject) throws JSONException {
        JSONArray myItems = jsonObject.getJSONArray("myBooks");
        JSONArray rentInItems = jsonObject.getJSONArray("rentIn");
        JSONArray rentOutItems = jsonObject.getJSONArray("rentOut");
        List<BookshelfItem> bookshelfItems = new ArrayList<>();
        for (int i=0; i<myItems.length(); ++i)
            bookshelfItems.add(new BorrowableBookshelfItem(myItems.getJSONObject(i)));
        for (int i=0; i<rentInItems.length(); ++i)
            bookshelfItems.add(new RentInBookshelfItem(rentInItems.getJSONObject(i)));
        for (int i=0; i<rentOutItems.length(); ++i)
            bookshelfItems.add(new RentOutBookshelfItem(rentOutItems.getJSONObject(i)));
        return bookshelfItems;
    }
}
