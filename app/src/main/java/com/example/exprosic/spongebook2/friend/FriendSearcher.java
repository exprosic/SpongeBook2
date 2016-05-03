package com.example.exprosic.spongebook2.friend;

import android.content.Context;
import android.util.Log;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.URLManager;
import com.example.exprosic.spongebook2.utils.net.StringFailureJsonResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by exprosic on 4/22/2016.
 */
public class FriendSearcher {
    public static final String TAG = FriendSearcher.class.getSimpleName();

    public interface OnFriendSearchedListener {
        void onFriendSearched(List<UserItem> userItems);
    }

    public void fetchUsers(final Context context, String pattern, final OnFriendSearchedListener listener) {
        MyApplication.getAuthorizedClient().get(context, URLManager.searchFriends(pattern), new StringFailureJsonResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONArray jsonArray) {
                try {
                    List<UserItem> userItems = new ArrayList<>(jsonArray.length());
                    for (int i=0; i<jsonArray.length(); ++i)
                        userItems.add(UserItem.fromJsonObject(jsonArray.getJSONObject(i)));
                    listener.onFriendSearched(userItems);
                } catch (JSONException e) {
                    Log.e(TAG, "wrong user list response", new Throwable());
                }
            }

            @Override
            public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                listener.onFriendSearched(null);
            }
        });
    }
}
