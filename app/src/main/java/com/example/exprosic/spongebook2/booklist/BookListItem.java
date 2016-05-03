package com.example.exprosic.spongebook2.booklist;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by exprosic on 5/3/2016.
 */
public class BookListItem {
    public static final String TAG = BookListItem.class.getSimpleName();

    public String mBookId;
    public boolean mBorrowable;
    public double mDeposit;
    public double mRental;

    public BookListItem(String bookId) {
        this(bookId, false, 0, 0);
    }

    public BookListItem(String bookId, boolean borrowable, double deposit, double rental) {
        mBookId = bookId;
        mBorrowable = borrowable;
        mDeposit = deposit;
        mRental = rental;
    }

    public static BookListItem fromJsonObject(JSONObject jsonObject) {
        try {
            return new BookListItem(jsonObject.getString("bookId"),
                    jsonObject.getBoolean("borrowable"),
                    jsonObject.getDouble("deposit"),
                    jsonObject.getDouble("rental"));
        } catch (JSONException e) {
            Log.e(TAG, "wrong BookListItem JSON format");
            return null;
        }
    }

    public static List<BookListItem> fromJsonArray(JSONArray jsonArray) {
        try {
            List<BookListItem> bookListItems = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); ++i)
                bookListItems.add(BookListItem.fromJsonObject(jsonArray.getJSONObject(i)));
            return bookListItems;
        } catch (JSONException e) {
            Log.e(TAG, "wrong BookListItems JSONArray format");
            return null;
        }
    }
}
