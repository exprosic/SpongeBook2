package com.example.exprosic.spongebook2.book;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.URLManager;
import com.example.exprosic.spongebook2.utils.Database;
import com.example.exprosic.spongebook2.utils.JSON;
import com.example.exprosic.spongebook2.utils.StringUtils;
import com.example.exprosic.spongebook2.utils.Sync;
import com.example.exprosic.spongebook2.utils.net.StringFailureJsonResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 *  Created by exprosic on 4/15/2016.
 */
public class BookProvider {
    private static final String TAG = BookProvider.class.getSimpleName();
    //    private static final int CACHED_BOOKS_NUMBER = 100;
    private static final String DB_NAME = "bookinfo.db";
    private static final int DB_VERSION = 1;

    public interface OnFetchedListener {
        void onBookFetched(BookItem bookItem);
    }

    private Sync.LoopRunnableThread mDbThread = Sync.newLoopRunnableThreadHandler();

    public void fetchBookById(final Context context, final String bookId, final OnFetchedListener listener) {
        mDbThread.addTask(new Runnable() {
            @Override
            public void run() {
                fetchBookByIdFromDb(bookId, new OnFetchedListener() {
                    @Override
                    public void onBookFetched(BookItem bookItem) {
                        if (bookItem != null) {
                            listener.onBookFetched(bookItem);
                        } else {
                            MyApplication.getUnauthorizedClient().get(context, URLManager.bookInfoFromId(bookId),
                                    responseHandlerWithListener(listener));
                        }
                    }
                });
            }
        });
    }

    public void fetchBookByIsbn(Context context, String isbn, OnFetchedListener listener) {
        MyApplication.getUnauthorizedClient().get(context, URLManager.bookInfoFromIsbn(isbn),
                responseHandlerWithListener(listener));
    }

    public void invalidateDb() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(BookInfosContract.SQL_CLEAR_TABLE);
        db.execSQL(BookUniInfoContract.SQL_CLEAR_TABLE);
    }

    private void fetchBookByIdFromDb(String bookId, OnFetchedListener listener) {
        //调用者负责处理线程问题
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = Database.rawQuery(db, BookUniInfoContract.SQL_QUERY_BY_BOOK_ID, bookId);
        BookItem bookItem = null;
        try {
            if (!cursor.moveToFirst())
                // 没有找到
                return;

            long _id = cursor.getLong(cursor.getColumnIndex(BookUniInfoContract._ID));
            String title = cursor.getString(cursor.getColumnIndex(BookUniInfoContract.COLUMN_NAME_BOOK_NAME));
            String imageUrl = StringUtils.emptyToNull(cursor.getString(cursor.getColumnIndex(BookUniInfoContract.COLUMN_NAME_IMAGE_URL)));
            cursor = Database.rawQuery(db, BookInfosContract.SQL_QUERY_BY_UNI_INFO_ID, _id);
            Map<String, String> infoMap = new HashMap<>(cursor.getCount());
            while (cursor.moveToNext())
                infoMap.put(cursor.getString(cursor.getColumnIndex(BookInfosContract.COLUMN_NAME_INFO_KEY)),
                        cursor.getString(cursor.getColumnIndex(BookInfosContract.COLUMN_NAME_INFO_VALUE)));

            bookItem = new BookItem(bookId, title, infoMap, imageUrl);
        } finally {
            cursor.close();
            db.close();
            listener.onBookFetched(bookItem);
        }
    }


    private void tryInsertBookIntoDb(BookItem bookItem) {
        // 一定不是主线程
        if (Thread.currentThread() == Looper.getMainLooper().getThread())
            Log.e(TAG, "inserting book into db on MAIN thread", new Throwable());

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            ContentValues values = new ContentValues();
            values.put(BookUniInfoContract.COLUMN_NAME_BOOK_ID, bookItem.mBookId);
            values.put(BookUniInfoContract.COLUMN_NAME_BOOK_NAME, bookItem.mTitle);
            values.put(BookUniInfoContract.COLUMN_NAME_IMAGE_URL, StringUtils.nullToEmpty(bookItem.mImageUrl));
            long _id = db.insert(BookUniInfoContract.TABLE_NAME, null, values); //如果已存在则会抛出异常

            db.beginTransaction();
            try {
                for (Map.Entry<String, String> entry : bookItem.mInfos.entrySet()) {
                    values = new ContentValues();
                    values.put(BookInfosContract.COLUMN_NAME_UNI_INFO_ID, _id);
                    values.put(BookInfosContract.COLUMN_NAME_INFO_KEY, entry.getKey());
                    values.put(BookInfosContract.COLUMN_NAME_INFO_VALUE, entry.getValue());
                    db.insert(BookInfosContract.TABLE_NAME, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteConstraintException e) {
            /* already existed*/
        } finally {
            db.close();
        }
    }

    private JsonHttpResponseHandler responseHandlerWithListener(final OnFetchedListener listener) {
        return new StringFailureJsonResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                try {
                    String title = jsonObject.getString("title");
                    String bookId = jsonObject.getString("bookid");
                    String imageUrl = jsonObject.optString("imageurl", null);
                    // /n0/太大，/n3/比较合适
                    if (imageUrl != null)
                        imageUrl = imageUrl.replace("/n0/", "/n3/");
                    Map<String, String> infoMap = JSON.toMap(jsonObject.getJSONObject("infos"));

                    BookItem bookItem = new BookItem(bookId, title, infoMap, imageUrl);
                    tryInsertBookIntoDb(bookItem);
                    listener.onBookFetched(bookItem);
                } catch (JSONException e) {
                    Log.e(TAG, "wrong BookItem format", e);
                }
            }

            @Override
            public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                listener.onBookFetched(null);
            }
        };
    }

    private static abstract class BookUniInfoContract implements BaseColumns {
        public static final String TABLE_NAME = "UniInfo";
        public static final String COLUMN_NAME_BOOK_ID = "BookId";
        public static final String COLUMN_NAME_BOOK_NAME = "BookName";
        public static final String COLUMN_NAME_IMAGE_URL = "ImageUrl";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%3$s TEXT UNIQUE, " +
                        "%4$s TEXT, " +
                        "%5$s TEXT);",
                TABLE_NAME, _ID, COLUMN_NAME_BOOK_ID, COLUMN_NAME_BOOK_NAME, COLUMN_NAME_IMAGE_URL);

        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE %s",
                TABLE_NAME);

        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s",
                TABLE_NAME);

        public static final String SQL_QUERY_BY_BOOK_ID = String.format(Locale.US,
                "SELECT * FROM %s WHERE %s=? LIMIT 1",
                TABLE_NAME, COLUMN_NAME_BOOK_ID);
    }

    private static abstract class BookInfosContract implements BaseColumns {
        public static final String TABLE_NAME = "Infos";
        public static final String COLUMN_NAME_UNI_INFO_ID = "UniInfoId";
        public static final String COLUMN_NAME_INFO_KEY = "InfoKey";
        public static final String COLUMN_NAME_INFO_VALUE = "InfoValue";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%3$s INTEGER, " +
                        "%4$s TEXT, " +
                        "%5$s TEXT, " +
                        "FOREIGN KEY(%3$s) REFERENCES %6$s(%7$s));",
                TABLE_NAME, _ID, COLUMN_NAME_UNI_INFO_ID, COLUMN_NAME_INFO_KEY, COLUMN_NAME_INFO_VALUE,
                BookUniInfoContract.TABLE_NAME, BookUniInfoContract._ID);

        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE %s",
                TABLE_NAME);

        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s",
                TABLE_NAME);

        public static final String SQL_QUERY_BY_UNI_INFO_ID = String.format(Locale.US,
                "SELECT * FROM %s WHERE %s=?",
                TABLE_NAME, COLUMN_NAME_UNI_INFO_ID);
    }

    private SQLiteOpenHelper mDbHelper = new SQLiteOpenHelper(MyApplication.getInstance(), DB_NAME, null, DB_VERSION) {
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BookUniInfoContract.SQL_CREATE_TABLE);
            db.execSQL(BookInfosContract.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(BookInfosContract.SQL_DROP_TABLE);
            db.execSQL(BookUniInfoContract.SQL_DROP_TABLE);
            onCreate(db);
        }
    };
}
