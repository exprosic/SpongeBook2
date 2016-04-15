package com.example.exprosic.spongebook2;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;

import java.util.Locale;

/**
 * Created by exprosic on 4/8/2016.
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static MyApplication getInstance() {
        assert myApplication != null;
        return myApplication;
    }

    private static final String PREF_TOKEN = "token";

    private static SharedPreferences getAuthorizePreference() {
        return PreferenceManager.getDefaultSharedPreferences(getInstance());
    }
    private static String getAuthorizeToken() {
        return getAuthorizePreference().getString(PREF_TOKEN, null);
    }

    private static void setAuthorizeToken(String token) {
        getAuthorizePreference().edit().putString(PREF_TOKEN, token).commit();
    }

    public static AsyncHttpClient getAuthorizedClient() {
        String token = getAuthorizeToken();
        if (token == null) {
            Log.e(TAG, "not authorized yet");
            return null;
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", "Token "+token);
        return client;
    }

    public static AsyncHttpClient getUnauthorizedClient() {
        return new AsyncHttpClient();
    }

    /***********/
    private int mMyUserId = -1;

    public static int getMyUserId() {
        int result = getInstance().mMyUserId;
        if (result < 0)
            new Throwable("myUserId not known yet").printStackTrace();
        return result;
    }

    public static void setMyUserId(int myUserId) {
        if (myUserId < 0)
            new Throwable(String.format(Locale.US, "setting illegal userId: %d", myUserId)).printStackTrace();
        getInstance().mMyUserId = myUserId;
    }

    public static boolean isMyself(int userId) {
        return getMyUserId() == userId;
    }

    public static void invalidateMyUserId() {
        getInstance().mMyUserId = -1;
    }
}
