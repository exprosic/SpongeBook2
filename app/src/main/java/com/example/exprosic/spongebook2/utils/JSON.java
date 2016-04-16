package com.example.exprosic.spongebook2.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by exprosic on 4/15/2016.
 */
public class JSON {
    public static Map<String,String> toMap(JSONObject jsonObject) throws JSONException {
        Map<String,String> map = new HashMap<>(jsonObject.length());
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, jsonObject.getString(key));
        }
        return map;
    }
}
