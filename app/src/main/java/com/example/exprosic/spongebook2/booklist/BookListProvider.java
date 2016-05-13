package com.example.exprosic.spongebook2.booklist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.URLManager;
import com.example.exprosic.spongebook2.utils.Database;
import com.example.exprosic.spongebook2.utils.Debugging;
import com.example.exprosic.spongebook2.utils.Sync;
import com.example.exprosic.spongebook2.utils.net.StringFailureJsonResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by exprosic on 4/15/2016.
 */
public class BookListProvider {
    private static final String TAG = BookListProvider.class.getSimpleName();
    private static final String PREF_IS_SYNCHRONIZED = "isBookListSynchronized";
    private static final String DB_NAME = "booklist.db";
    private static final int DB_VERSION = 5;

    private long mLastRefreshTimeStamp;

    public interface OnFetchedListener {
        void onBookListFetched(List<BookshelfItem> bookshelfItems);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fetchBookListFromDb(userId, listener);
                    }
                }).run();
                //应该是.start()，但会RE:Can't create handler inside thread that has not called Looper.prepare()。暂时先这样
            } else {
                downloadBookList(context, userId, new OnFetchedListener() {
                    @Override
                    public void onBookListFetched(List<BookshelfItem> bookshelfItems) {
                        mLastRefreshTimeStamp = Sync.newTimeStamp();
                        MyApplication.putGlobalPreferencesBoolean(PREF_IS_SYNCHRONIZED, true);
                        if (MyApplication.isMyself(userId))
                            insertBookListIntoDb(bookshelfItems);
                        listener.onBookListFetched(bookshelfItems);
                    }
                });
            }
        } else {
            downloadBookList(context, userId, listener);
        }
    }

    private void downloadBookList(Context context, int userId, final OnFetchedListener listener) {
        MyApplication.getUnauthorizedClient().get(context, URLManager.Bookshelf.get(userId), new StringFailureJsonResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                try {
                    listener.onBookListFetched(BookshelfItem.getCollectionFromJson(jsonObject));
                } catch (JSONException e) {
                    Log.e(TAG, "wrong booklist format", e);
                }
            }
        });
    }

    public void postBookList(final Context context, final List<String> bookIds, final OnBookListUpdatedListener listener) {
        MyApplication.getAuthorizedClient().post(context, URLManager.Bookshelf.postIds.URL,
                URLManager.Bookshelf.postIds.params(bookIds), new StringFailureJsonResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                        int insertedCount, ignoredCount;
                        try {
                            insertedCount = jsonObject.getInt("insertedCount");
                            ignoredCount = jsonObject.getInt("ignoredCount");
                            mLastRefreshTimeStamp = Sync.newTimeStamp();
                            insertBookIdsIntoDb(MyApplication.getMyUserId(), bookIds);
                            listener.onBookListUpdated(insertedCount, ignoredCount);
                        } catch (JSONException e) {
                            Log.e(TAG, "wrong update booklist response format", e);
                            Debugging.myAssertFalse();
                        }
                    }
                });
    }

    public void syncDb(List<BookshelfItem> bookshelfItems) {
        mLastRefreshTimeStamp = Sync.newTimeStamp();
        insertBookListIntoDb(bookshelfItems);
    }

    public void invalidateDb() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(BorrowableBookshelfItem.DbContract.SQL_CLEAR_TABLE);
        db.execSQL(RentInBookshelfItem.DbContract.SQL_CLEAR_TABLE);
        db.execSQL(RentOutBookshelfItem.DbContract.SQL_CLEAR_TABLE);
        MyApplication.getGlobalPreferences().edit().remove(PREF_IS_SYNCHRONIZED).commit();
    }

    private void fetchBookListFromDb(int userId, OnFetchedListener listener) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            List<BookshelfItem> bookshelfItems = new ArrayList<>();
            Cursor cursor;

            cursor = Database.select(db, BorrowableBookshelfItem.DbContract.TABLE_NAME)
                    .where(BorrowableBookshelfItem.DbContract.COLUMN_NAME_USER_ID + "=?", userId).query();
            while (cursor.moveToNext())
                bookshelfItems.add(BorrowableBookshelfItem.fromCursor(cursor));
            cursor.close();

            cursor = Database.select(db, RentInBookshelfItem.DbContract.TABLE_NAME)
                    .where(RentInBookshelfItem.DbContract.COLUMN_NAME_USER_ID + "=?", userId).query();
            while (cursor.moveToNext()) {
                bookshelfItems.add(RentInBookshelfItem.fromCursor(cursor));
            }
            cursor.close();

            cursor = Database.select(db, RentOutBookshelfItem.DbContract.TABLE_NAME)
                    .where(RentOutBookshelfItem.DbContract.COLUMN_NAME_USER_ID + "=?", userId).query();
            while (cursor.moveToNext()) {
                bookshelfItems.add(RentOutBookshelfItem.fromCursor(cursor));
            }
            cursor.close();

            listener.onBookListFetched(bookshelfItems);
        } finally {
            db.close();
        }
    }

    private void insertBookIdsIntoDb(int userId, List<String> bookIds) {
        List<BookshelfItem> bookshelfItems = new ArrayList<>(bookIds.size());
        for (int i=0; i<bookIds.size(); ++i)
            bookshelfItems.add(new BorrowableBookshelfItem(userId, bookIds.get(i)));
        insertBookListIntoDb(bookshelfItems);
    }

    public interface onBookshelfItemUpdatedListener {
        void onBookshelfItemUpdated(boolean succeeded);
    }

    public void updateBookshelfItem(Context context, final BorrowableBookshelfItem bookshelfItem, final onBookshelfItemUpdatedListener listener) {
        MyApplication.getAuthorizedClient().post(context, URLManager.Bookshelf.postItem.URL,
                URLManager.Bookshelf.postItem.params(bookshelfItem), new StringFailureJsonResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                        if (jsonObject.has("error")) {
                            listener.onBookshelfItemUpdated(false);
                        } else {
                            mLastRefreshTimeStamp = Sync.newTimeStamp();
                            SQLiteDatabase db = mDbHelper.getWritableDatabase();
                            bookshelfItem.insertIntoDb(db);
                            listener.onBookshelfItemUpdated(true);
                        }
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                        listener.onBookshelfItemUpdated(false);
                    }
                });
    }

    private void insertBookListIntoDb(List<BookshelfItem> bookshelfItems) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        for (BookshelfItem bookshelfItem : bookshelfItems) {
            bookshelfItem.insertIntoDb(db);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private SQLiteOpenHelper mDbHelper = new SQLiteOpenHelper(MyApplication.getInstance(), DB_NAME, null, DB_VERSION) {
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BorrowableBookshelfItem.DbContract.SQL_CREATE_TABLE);
            db.execSQL(RentInBookshelfItem.DbContract.SQL_CREATE_TABLE);
            db.execSQL(RentOutBookshelfItem.DbContract.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(BorrowableBookshelfItem.DbContract.SQL_DROP_TABLE);
            db.execSQL(RentInBookshelfItem.DbContract.SQL_DROP_TABLE);
            db.execSQL(RentOutBookshelfItem.DbContract.SQL_DROP_TABLE);
            onCreate(db);
        }
    };
}
