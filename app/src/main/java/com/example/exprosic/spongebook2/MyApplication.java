package com.example.exprosic.spongebook2;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;

/**
 * Created by exprosic on 4/8/2016.
 */
public class MyApplication extends Application {
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

    private static String PREF_TOKEN;

    private SharedPreferences getAuthorizePreference() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
    private String getAuthorizeToken() {
        String token = getAuthorizePreference().getString(PREF_TOKEN, null);
        return token;
    }

    private void setAuthorizeToken(String token) {
        getAuthorizePreference().edit().putString(PREF_TOKEN, token).commit();
    }

    public static AsyncHttpClient getAuthorizedClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", "Token "+getInstance().getAuthorizeToken());
        return client;
    }

    public static AsyncHttpClient getUnauthorizedClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        return client;
    }
}
