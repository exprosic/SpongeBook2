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
    private static final String DB_NAME = "booklist.db";
    private static final int DB_VERSION = 4;

    private long mLastRefreshTimeStamp;

    public interface OnFetchedListener {
        void onBookListFetched(List<BookListItem> bookListItems);
    }

    public interface OnBookListUpdatedListener {
        void onBookListUpdated(int insertedCount, int ignoredCount);
    }

    public long getTimeStamp() {
        return mLastRefreshTimeStamp;
    }

    public void fetchBookList(Context context, final int userId, final OnFetchedListener listener) {
        if (MyApplication.isMyself(userId)) {
            if (MyApplication.getGlobalPreferences().getBoolean(PREF_IS_SYNCHRONIZED, false)) {
                fetchBookListFromDb(userId, listener);
            } else {
                downloadBookList(context, userId, new OnFetchedListener() {
                    @Override
                    public void onBookListFetched(List<BookListItem> bookListItems) {
                        mLastRefreshTimeStamp = Sync.newTimeStamp();
                        MyApplication.putGlobalPreferencesBoolean(PREF_IS_SYNCHRONIZED, true);
                        if (MyApplication.isMyself(userId))
                            insertBookListIntoDb(userId, bookListItems);
                        listener.onBookListFetched(bookListItems);
                    }
                });
            }
        } else {
            downloadBookList(context, userId, listener);
        }
    }

    private void downloadBookList(Context context, int userId, final OnFetchedListener listener) {
        MyApplication.getUnauthorizedClient().get(context, URLManager.BookList.get(userId), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONArray jsonArray) {
                List<BookListItem> bookListItems = new ArrayList<>(jsonArray.length());
                try {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        BookListItem bookListItem = BookListItem.fromJsonObject(jsonObject);
                        bookListItems.add(bookListItem);
                    }
                    listener.onBookListFetched(bookListItems);
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
                        insertBookIdsIntoDb(MyApplication.getMyUserId(), bookIds);
                        listener.onBookListUpdated(insertedCount, ignoredCount);
                    }
                });
    }

    public void syncDb(List<BookListItem> bookListItems) {
        mLastRefreshTimeStamp = Sync.newTimeStamp();
        insertBookListIntoDb(MyApplication.getMyUserId(), bookListItems);
    }

    public void invalidateDb() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(DbContract.SQL_CLEAR_TABLE);
        MyApplication.getGlobalPreferences().edit().remove(PREF_IS_SYNCHRONIZED).commit();
    }

    private void fetchBookListFromDb(int userId, OnFetchedListener listener) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = Database.rawQuery(db, DbContract.SQL_QUERY_BY_USER_ID, userId);
        List<BookListItem> bookListItems = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String bookId = Database.getStringFromCursor(cursor, DbContract.COLUMN_NAME_BOOK_ID);
            boolean borrowable = Database.getIntFromCursor(cursor, DbContract.COLUMN_NAME_BORROWABLE) == 1;
            double deposit = 0;
            double rental = 0;
            if (borrowable) {
                deposit = Database.getDoubleFromCursor(cursor, DbContract.COLUMN_NAME_DEPOSIT);
                rental = Database.getDoubleFromCursor(cursor, DbContract.COLUMN_NAME_RENTAL);
            }
            BookListItem bookListItem = new BookListItem(bookId, borrowable, deposit, rental);
            bookListItems.add(bookListItem);
        }
        cursor.close();
        listener.onBookListFetched(bookListItems);
    }

    private void insertBookIdsIntoDb(int userId, List<String> bookIds) {
        List<BookListItem> bookListItems = new ArrayList<>(bookIds.size());
        for (int i=0; i<bookIds.size(); ++i)
            bookListItems.add(new BookListItem(bookIds.get(i), false, 0., 0.));
        insertBookListIntoDb(userId, bookListItems);
    }

    private void insertBookListIntoDb(int userId, List<BookListItem> bookListItems) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        for (BookListItem bookListItem: bookListItems) {
            ContentValues values = new ContentValues();
            values.put(DbContract.COLUMN_NAME_USER_ID, userId);
            values.put(DbContract.COLUMN_NAME_BOOK_ID, bookListItem.mBookId);
            values.put(DbContract.COLUMN_NAME_BORROWABLE, bookListItem.mBorrowable?1:0);
            values.put(DbContract.COLUMN_NAME_DEPOSIT, bookListItem.mDeposit);
            values.put(DbContract.COLUMN_NAME_RENTAL, bookListItem.mRental);
            db.insertWithOnConflict(DbContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private static abstract class DbContract implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_USER_ID = "UserId";
        public static final String COLUMN_NAME_BOOK_ID = "BookId";
        public static final String COLUMN_NAME_BORROWABLE = "Borrowable";
        public static final String COLUMN_NAME_DEPOSIT = "Deposit";
        public static final String COLUMN_NAME_RENTAL = "Rental";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%3$s INTEGER, " +
                        "%4$s TEXT NOT NULL, " +
                        "%5$s INTEGER, " +
                        "%6$s REAL, " +
                        "%7$s REAL, " +
                        "UNIQUE(%3$s,%4$s));",
                TABLE_NAME, _ID, COLUMN_NAME_USER_ID, COLUMN_NAME_BOOK_ID,
                COLUMN_NAME_BORROWABLE, COLUMN_NAME_DEPOSIT, COLUMN_NAME_RENTAL);

        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE %s;",
                TABLE_NAME);

        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s",
                TABLE_NAME);

        // TODO: use SQLiteDatabase#query rather than #rawQuery
        public static final String SQL_QUERY_BY_USER_ID = String.format(Locale.US,
                "SELECT * FROM %s WHERE %s=?",
                TABLE_NAME, COLUMN_NAME_USER_ID);
    }

    private SQLiteOpenHelper mDbHelper = new SQLiteOpenHelper(MyApplication.getInstance(), DB_NAME, null, DB_VERSION) {
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DbContract.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DbContract.SQL_DROP_TABLE);
            onCreate(db);
        }
    };
}
