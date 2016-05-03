package com.example.exprosic.spongebook2.friend;

import android.util.Log;

import com.example.exprosic.spongebook2.booklist.BookListItem;
import com.example.exprosic.spongebook2.utils.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by exprosic on 4/18/2016.
 */
public class UserItem {
    public static final String TAG = UserItem.class.getSimpleName();

    public int mUserId;
    public String mNick;
    public int mGender;
    public String mLocation;
    public List<BookListItem> mPreviewBookItems;

    public UserItem(int userId, String nick, int gender, String location, List<BookListItem> previewBookItems) {
        mUserId = userId;
        mNick = nick;
        mGender = gender;
        mLocation = location;
        mPreviewBookItems = previewBookItems;
    }

    public static UserItem fromJsonObject(JSONObject jsonObject) {
        try {
            int userId = jsonObject.getInt("userId");
            String nick = jsonObject.getString("nick");
            int gender = jsonObject.getInt("gender");
            String location = jsonObject.getString("location");
            List<BookListItem> previewBookItems = BookListItem.fromJsonArray(jsonObject.getJSONArray("previewBookItems"));
            return new UserItem(userId, nick, gender, location, previewBookItems);
        } catch (JSONException e) {
            Log.e(TAG, "wrong UserItem JSON format", new Throwable());
            return null;
        }
    }
}
