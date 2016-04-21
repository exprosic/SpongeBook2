package com.example.exprosic.spongebook2.friend;

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
import com.example.exprosic.spongebook2.utils.net.StringFailureJsonResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by exprosic on 4/20/2016.
 */
public class FriendListProvider {
    private static final String TAG = FriendListProvider.class.getSimpleName();
    private static final String PREF_IS_SYNCHRONIZED = "isFriendListSynchronized";
    private static final String DB_NAME = "friendlist.db";
    private static final int DB_VERSION = 1;

    private long mLastRefreshTimeStamp;
    public long getLastRefreshTimeStamp() {
        return mLastRefreshTimeStamp;
    }

    public interface OnFriendListFetchedListener {
        void onFriendListFetched(List<UserItem> userIds);
    }

    public void fetchFriendList(Context context, final OnFriendListFetchedListener listener) {
        if (MyApplication.getGlobalPreferences().getBoolean(PREF_IS_SYNCHRONIZED, false)) {
            fetchFriendListFromDb(listener);
        } else {
            downloadFriendList(context, new OnFriendListFetchedListener() {
                @Override
                public void onFriendListFetched(List<UserItem> userItems) {
                    mLastRefreshTimeStamp = Sync.newTimeStamp();
                    MyApplication.putGlobalPreferencesBoolean(PREF_IS_SYNCHRONIZED, true);
//                    本地数据库要么不存在，要么存着最新数据，所以不用清除
                    insertFriendListToDb(userItems);
                    listener.onFriendListFetched(userItems);
                }
            });
        }
    }

    private void fetchFriendListFromDb(OnFriendListFetchedListener listener) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = Database.rawQuery(db, FriendInfoDbContract.SQL_QUERY_FRIEND_INFO);
        List<UserItem> userItems = null;
        try {
            userItems = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                int userId = Database.getIntFromCursor(cursor, FriendInfoDbContract.COLUMN_NAME_FRIEND_USER_ID);
                String nick = Database.getStringFromCursor(cursor, FriendInfoDbContract.COLUMN_NAME_FRIEND_NICK);
                int gender = Database.getIntFromCursor(cursor, FriendInfoDbContract.COLUMN_NAME_GENDER);
                String location = Database.getStringFromCursor(cursor, FriendInfoDbContract.COLUMN_NAME_LOCATION);

                Cursor bookCursor = Database.rawQuery(db, FriendPreviewBookIdsDbContract.SQL_QUERY_BOOK_ID, userId);
                List<String> bookIds = new ArrayList<>(bookCursor.getCount());
                while (bookCursor.moveToNext())
                    bookIds.add(Database.getStringFromCursor(bookCursor, FriendPreviewBookIdsDbContract.COLUMN_NAME_BOOK_ID));

                userItems.add(new UserItem(userId, nick, gender, location, bookIds));
            }
        } finally {
            cursor.close();
            db.close();
            listener.onFriendListFetched(userItems);
        }
    }

    private void insertFriendListToDb(List<UserItem> userItems) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (UserItem userItem : userItems) {
                ContentValues values = new ContentValues();
                values.put(FriendInfoDbContract.COLUMN_NAME_FRIEND_USER_ID, userItem.userId);
                values.put(FriendInfoDbContract.COLUMN_NAME_FRIEND_NICK, userItem.nick);
                values.put(FriendInfoDbContract.COLUMN_NAME_GENDER, userItem.gender);
                values.put(FriendInfoDbContract.COLUMN_NAME_LOCATION, userItem.location);
                db.insert(FriendInfoDbContract.TABLE_NAME, null, values);

                for (String bookId: userItem.previewBookIds) {
                    values = new ContentValues();
                    values.put(FriendPreviewBookIdsDbContract.COLUMN_NAME_USER_ID, userItem.userId);
                    values.put(FriendPreviewBookIdsDbContract.COLUMN_NAME_BOOK_ID, bookId);
                    db.insert(FriendPreviewBookIdsDbContract.TABLE_NAME, null, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void downloadFriendList(Context context, final OnFriendListFetchedListener listener) {
        MyApplication.getAuthorizedClient().get(context, URLManager.friendList, new StringFailureJsonResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONArray jsonArray) {
                try {
                    List<UserItem> userItems = new ArrayList<>(jsonArray.length());
                    for (int i=0; i<jsonArray.length(); ++i)
                        userItems.add(UserItem.parseJSON(jsonArray.getJSONObject(i)));
                    listener.onFriendListFetched(userItems);
                } catch (JSONException e) {
                    Log.e(TAG, "wrong friend list response", new Throwable());
                }
            }
        });
    }

    public void invalidateDb() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(FriendInfoDbContract.SQL_CLEAR_TABLE);
        db.execSQL(FriendPreviewBookIdsDbContract.SQL_CLEAR_TABLE);
        MyApplication.getGlobalPreferences().edit().remove(PREF_IS_SYNCHRONIZED).commit();
    }

    private static class FriendInfoDbContract implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_FRIEND_USER_ID = "UserId";
        public static final String COLUMN_NAME_FRIEND_NICK = "Nick";
        public static final String COLUMN_NAME_GENDER = "Gender";
        public static final String COLUMN_NAME_LOCATION = "Location";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s INTEGER UNIQUE, " +
                        "%s TEXT, " +
                        "%s INTEGER, " +
                        "%s TEXT);",
                TABLE_NAME, _ID, COLUMN_NAME_FRIEND_USER_ID, COLUMN_NAME_FRIEND_NICK, COLUMN_NAME_GENDER, COLUMN_NAME_LOCATION);
        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE %s;",
                TABLE_NAME);
        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s;",
                TABLE_NAME);
        public static final String SQL_QUERY_FRIEND_INFO = String.format(Locale.US,
                "SELECT * FROM %s;",
                TABLE_NAME);
    }

    private static class FriendPreviewBookIdsDbContract implements BaseColumns {
        public static final String TABLE_NAME = "preview";
        public static final String COLUMN_NAME_USER_ID = "UserId";
        public static final String COLUMN_NAME_BOOK_ID = "BookId";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s INTEGER, " +
                        "%s TEXT);",
                TABLE_NAME, _ID, COLUMN_NAME_USER_ID, COLUMN_NAME_BOOK_ID);
        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE %s;",
                TABLE_NAME);
        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s;",
                TABLE_NAME);
        public static final String SQL_QUERY_BOOK_ID = String.format(Locale.US,
                "SELECT * FROM %s WHERE %s=?;",
                TABLE_NAME, COLUMN_NAME_USER_ID);
    }

    public interface OnFriendRequestsFetchedListener {
        void onFriendRequestsFetched(List<UserItem> userItems);
    }

    public void fetchFriendRequests(Context context, final OnFriendRequestsFetchedListener listener) {
        MyApplication.getAuthorizedClient().post(context, URLManager.friendRequests.URL, URLManager.friendRequests.params(),
                new StringFailureJsonResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONArray jsonArray) {
                        try {
                            List<UserItem> userItems = new ArrayList<>(jsonArray.length());
                            for (int i=0; i<jsonArray.length(); ++i)
                                userItems.add(UserItem.parseJSON(jsonArray.getJSONObject(i)));
                            listener.onFriendRequestsFetched(userItems);
                        } catch (JSONException e) {
                            Log.e(TAG, "friend requests response error", new Throwable());
                        }
                    }
                });
    }

    public interface OnFriendRequestedListener {
        void onFriendRequested(boolean isSucceeded);
    }

    public void newFriendRequest(Context context, int userId, final OnFriendRequestedListener listener) {
        MyApplication.getAuthorizedClient().post(context, URLManager.requestFriend.URL(userId), URLManager.requestFriend.params(),
                new StringFailureJsonResponseHandler() {
                   @Override
                    public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                       listener.onFriendRequested(true);
                   }
                    @Override
                    public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                        listener.onFriendRequested(false);
                    }
                });
    }

    public interface OnFriendAcceptedListener {
        void onFriendAccepted(boolean isSucceeded);
    }

    public void newFriendAccept(Context context, int userId, final OnFriendAcceptedListener listener) {
        MyApplication.getAuthorizedClient().post(context, URLManager.acceptFriend.URL(userId), URLManager.acceptFriend.params(),
                new StringFailureJsonResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject jsonObject) {
                        listener.onFriendAccepted(true);
                    }
                    @Override
                    public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                        listener.onFriendAccepted(false);
                    }
                });
    }

    private SQLiteOpenHelper mDbHelper = new SQLiteOpenHelper(MyApplication.getInstance(), DB_NAME, null, DB_VERSION) {
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FriendInfoDbContract.SQL_CREATE_TABLE);
            db.execSQL(FriendPreviewBookIdsDbContract.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(FriendInfoDbContract.SQL_DROP_TABLE);
            db.execSQL(FriendPreviewBookIdsDbContract.SQL_DROP_TABLE);
            onCreate(db);
        }
    };
}