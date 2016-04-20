package com.example.exprosic.spongebook2.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by exprosic on 4/15/2016.
 */
public class JSON {
    public static class JSONObjectIterator implements Iterable<String> {
        private JSONObject mJSONObject;
        public JSONObjectIterator(JSONObject jsonObject) {
            mJSONObject = jsonObject;
        }
        @Override
        public Iterator<String> iterator() {
            return mJSONObject.keys();
        }
    }

    public static Map<String,String> toMap(JSONObject jsonObject) throws JSONException {
        Map<String,String> map = new HashMap<>(jsonObject.length());
        for (String key: new JSONObjectIterator(jsonObject))
            map.put(key, jsonObject.getString(key));
        return map;
    }

    public static List<String> toStringList(JSONArray jsonArray) throws JSONException {
        List<String> result = new ArrayList<>(jsonArray.length());
        for (int i=0; i<jsonArray.length(); ++i)
            result.add(jsonArray.getString(i));
        return result;
    }
}
