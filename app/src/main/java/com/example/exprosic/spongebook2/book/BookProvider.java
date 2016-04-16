package com.example.exprosic.spongebook2.book;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.URLManager;
import com.example.exprosic.spongebook2.utils.JSON;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by exprosic on 4/15/2016.
 */
public class BookProvider {
    private static final String TAG = BookProvider.class.getSimpleName();
    private static final int CACHED_BOOKS_NUMBER = 100;

    LruCache<String,BookItem> mLruCache = new LruCache<>(CACHED_BOOKS_NUMBER);

    public interface OnBookFetchedListener {
        void onBookFetched(BookItem bookItem);
    }

    public void fetchBookById(Context context, String bookId, OnBookFetchedListener listener) {
        BookItem bookItem = mLruCache.get(bookId);
        if (bookItem != null) {
            listener.onBookFetched(bookItem);
            return;
        }

        MyApplication.getUnauthorizedClient().get(context, URLManager.bookInfoFromId(bookId), responseHandlerWithListener(listener));
    }

    public void fetchBookByIsbn(Context context, String isbn, OnBookFetchedListener listener) {
        MyApplication.getUnauthorizedClient().get(context, URLManager.bookInfoFromIsbn(isbn), responseHandlerWithListener(listener));
    }

    private JsonHttpResponseHandler responseHandlerWithListener(final OnBookFetchedListener listener) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                try {
                    String title = jsonObject.getString("title");
                    String bookId = jsonObject.getString("bookid");
                    String imageUrl = jsonObject.optString("imageurl", null);
                    Map<String, String> infoMap = JSON.toMap(jsonObject.getJSONObject("infos"));

                    BookItem bookItem = new BookItem(bookId, title, infoMap, imageUrl);
                    mLruCache.put(bookId, bookItem);
                    listener.onBookFetched(bookItem);
                } catch (JSONException e) {
                    Log.e(TAG, "wrong BookItem format", e);
                }
            }
        };
    }
}
