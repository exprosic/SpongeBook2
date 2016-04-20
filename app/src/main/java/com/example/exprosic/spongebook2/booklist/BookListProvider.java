package com.example.exprosic.spongebook2.booklist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.URLManager;
import com.example.exprosic.spongebook2.utils.Database;
import com.example.exprosic.spongebook2.utils.Sync;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by exprosic on 4/15/2016.
 */
public class BookListProvider {
    private static final String TAG = BookListProvider.class.getSimpleName();
    private static final String PREF_IS_SYNCHRONIZED = "isBookListSynchronized";

    private long mLastRefreshTimeStamp;

    public interface OnBookListFetchedListener {
        void onBookListFetched(List<String> bookIds);
    }

    public interface OnBookListUpdatedListener {
        void onBookListUpdated(int insertedCount, int ignoredCount);
    }

    public long getTimeStamp() {
        return mLastRefreshTimeStamp;
    }

    public void fetchBookList(Context context, final int userId, final OnBookListFetchedListener listener) {
        if (MyApplication.isMyself(userId)) {
            if (MyApplication.getGlobalPreferences().getBoolean(PREF_IS_SYNCHRONIZED, false)) {
                fetchBookListFromDb(userId, listener);
            } else {
                downloadBookList(context, userId, new OnBookListFetchedListener() {
                    @Override
                    public void onBookListFetched(List<String> bookIds) {
                        mLastRefreshTimeStamp = Sync.newTimeStamp();
                        MyApplication.putGlobalPreferencesBoolean(PREF_IS_SYNCHRONIZED, true);
                        if (MyApplication.isMyself(userId))
                            insertBookListIntoDb(userId, bookIds);
                        listener.onBookListFetched(bookIds);
                    }
                });
            }
        } else {
            downloadBookList(context, userId, listener);
        }
    }

    private void downloadBookList(Context context, int userId, final OnBookListFetchedListener listener) {
        MyApplication.getUnauthorizedClient().get(context, URLManager.BookList.get(userId), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONArray jsonArray) {
                List<String> bookIds = new ArrayList<>(jsonArray.length());
                try {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        bookIds.add(jsonArray.getString(i));
                    }
                    listener.onBookListFetched(bookIds);
                } catch (JSONException e) {
                    Log.e(TAG, "wrong booklist format", e);
                }
            }
        });
    }

    public void postBookList(final Context context, final List<String> bookIds, final OnBookListUpdatedListener listener) {
        MyApplication.getAuthorizedClient().post(context, URLManager.BookList.post.URL,
                URLManager.BookList.post.params(bookIds), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                        int insertedCount, ignoredCount;
                        try {
                            insertedCount = jsonObject.getInt("insertedCount");
                            ignoredCount = jsonObject.getInt("ignoredCount");
                        } catch (JSONException e) {
                            Log.e(TAG, "wrong update booklist response format", e);
                            throw new AssertionError();
                        }
                        mLastRefreshTimeStamp = Sync.newTimeStamp();
                        insertBookListIntoDb(MyApplication.getMyUserId(), bookIds);
                        listener.onBookListUpdated(insertedCount, ignoredCount);
                    }
                });
    }

    public void syncDb(List<String> bookIds) {
        mLastRefreshTimeStamp = Sync.newTimeStamp();
        insertBookListIntoDb(MyApplication.getMyUserId(), bookIds);
    }

    public void invalidateDb() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(BookListDbContract.SQL_CLEAR_TABLE);
        MyApplication.getGlobalPreferences().edit().remove(PREF_IS_SYNCHRONIZED).commit();
    }

    private void fetchBookListFromDb(int userId, OnBookListFetchedListener listener) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = Database.rawQuery(db, BookListDbContract.SQL_QUERY_BOOK_ID, userId);
        List<String> bookIds = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            bookIds.add(cursor.getString(cursor.getColumnIndex(BookListDbContract.COLUMN_NAME_BOOK_ID)));
        }
        cursor.close();
        listener.onBookListFetched(bookIds);
    }

    private void insertBookListIntoDb(int userId, List<String> bookIds) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        for (String bookId : bookIds) {
            ContentValues values = new ContentValues();
            values.put(BookListDbContract.COLUMN_NAME_USER_ID, userId);
            values.put(BookListDbContract.COLUMN_NAME_BOOK_ID, bookId);
            db.insertWithOnConflict(BookListDbContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private static final String DB_NAME = "booklist.db";
    private static final int DB_VERSION = 3;

    private static abstract class BookListDbContract implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_USER_ID = "UserId";
        public static final String COLUMN_NAME_BOOK_ID = "BookId";

        public static final String SQL_CREATE = String.format(Locale.US,
                "CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%3$s INTEGER, " +
                        "%4$s TEXT, " +
                        "UNIQUE(%3$s,%4$s));",
                TABLE_NAME, _ID, COLUMN_NAME_USER_ID, COLUMN_NAME_BOOK_ID);

        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE %s;",
                TABLE_NAME);

        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s",
                TABLE_NAME);

        public static final String SQL_QUERY_BOOK_ID = String.format(Locale.US,
                "SELECT %s FROM %s WHERE %s=?",
                COLUMN_NAME_BOOK_ID, TABLE_NAME, COLUMN_NAME_USER_ID);
    }

    private SQLiteOpenHelper mDbHelper = new SQLiteOpenHelper(MyApplication.getInstance(), DB_NAME, null, DB_VERSION) {
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BookListDbContract.SQL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(BookListDbContract.SQL_DROP_TABLE);
            onCreate(db);
        }
    };
}
