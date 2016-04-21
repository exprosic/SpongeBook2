package com.example.exprosic.spongebook2;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.booklist.BookListProvider;
import com.example.exprosic.spongebook2.friend.FriendListProvider;
import com.example.exprosic.spongebook2.friend.FriendSearcher;
import com.loopj.android.http.AsyncHttpClient;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by exprosic on 4/8/2016.
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static final String PREF_TOKEN = "token";
    private static final String PREF_USER_ID = "userId";

    private static final int POOL_SIZE = 5;

    private static MyApplication myApplication;
    private BookListProvider mBookListProvider;
    private BookProvider mBookProvider;
    private FriendListProvider mFriendListProvider;
    private FriendSearcher mFriendSearcher;
    private ExecutorService mExecutor; //给AsyncHttpClient用的有限大小线程池，不然不限制线程数的话会堵

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        mBookListProvider = new BookListProvider();
        mBookProvider = new BookProvider();
        mFriendListProvider = new FriendListProvider();
        mFriendSearcher = new FriendSearcher();
        mExecutor = Executors.newFixedThreadPool(POOL_SIZE);

        setupPicasso(this);
    }

    private void setupPicasso(Context context) {
        final int cacheSize = 10 * 1024 * 1024;
        Downloader downloader = new OkHttpDownloader(context, cacheSize);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(downloader);
        Picasso.setSingletonInstance(builder.build());
    }

    public static MyApplication getInstance() {
        assert myApplication != null;
        return myApplication;
    }

    public static BookListProvider getBookListProvider() {
        return myApplication.mBookListProvider;
    }

    public static BookProvider getBookProvider() {
        return myApplication.mBookProvider;
    }

    public static FriendListProvider getFriendListProvider() {
        return myApplication.mFriendListProvider;
    }

    public static FriendSearcher getFriendSearcher() {
        return myApplication.mFriendSearcher;
    }

    // preferences

    public static SharedPreferences getGlobalPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getInstance());
    }

    public static void putGlobalPreferencesBoolean(String key, boolean val) {
        SharedPreferences.Editor editor = getGlobalPreferences().edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    // authorization
    public static String getAuthorizeToken() {
        return getGlobalPreferences().getString(PREF_TOKEN, null);
    }

    public static void setAuthorizeToken(String token) {
        getGlobalPreferences().edit().putString(PREF_TOKEN, token).commit();
    }

    @NonNull
    public static AsyncHttpClient getAuthorizedClient() {
        String token = getAuthorizeToken();
        if (token == null) {
            Log.e(TAG, "not authorized yet", new Throwable());
            throw new AssertionError();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.setThreadPool(getInstance().mExecutor);
        client.addHeader("Authorization", "Token "+token);
        return client;
    }

    public static AsyncHttpClient getUnauthorizedClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setThreadPool(getInstance().mExecutor);
        return client;
    }

    // userId

    public static int getMyUserId() {
        int result = getGlobalPreferences().getInt(PREF_USER_ID, -1);
        if (result < 0)
            Log.e(TAG, "myUserId not known yet", new Throwable());
        return result;
    }

    public static void setMyUserId(int myUserId) {
        if (myUserId < 0)
            Log.d(TAG, String.format(Locale.US, "setting illegal userId: %d", myUserId), new Throwable());
        getGlobalPreferences().edit().putInt(PREF_USER_ID, myUserId).commit();
    }

    public static boolean isMyself(int userId) {
        return getMyUserId() == userId;
    }

    public static void invalidateSession() {
        getGlobalPreferences().edit().remove(PREF_TOKEN).remove(PREF_USER_ID).commit();
        getBookListProvider().invalidateDb();
//        getBookProvider().invalidateDb(); // 没必要抹掉所有本地书籍缓存
        getFriendListProvider().invalidateDb();
    }
}
