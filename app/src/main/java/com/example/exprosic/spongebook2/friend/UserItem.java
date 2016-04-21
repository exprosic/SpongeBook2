package com.example.exprosic.spongebook2.friend;

import android.util.Log;

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

    public int userId;
    public String nick;
    public int gender;
    public String location;
    public List<String> previewBookIds;

    public UserItem(int userId, String nick, int gender, String location, List<String> previewBookIds) {
        this.userId = userId;
        this.nick = nick;
        this.gender = gender;
        this.location = location;
        this.previewBookIds = previewBookIds;
    }

    public static UserItem parseJSON(JSONObject jsonObject) {
        try {
            int userId = jsonObject.getInt("userId");
            String nick = jsonObject.getString("nick");
            int gender = jsonObject.getInt("gender");
            String locatoin = jsonObject.getString("location");
            List<String> previewBookIds = JSON.toStringList(jsonObject.getJSONArray("previewBookIds"));
            return new UserItem(userId, nick, gender, locatoin, previewBookIds);
        } catch (JSONException e) {
            Log.e(TAG, "wrong UserItem JSON format", new Throwable());
            return null;
        }
    }
}
