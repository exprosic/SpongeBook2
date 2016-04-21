package com.example.exprosic.spongebook2.utils.net;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by exprosic on 4/17/2016.
 */
public class StringFailureJsonResponseHandler extends JsonHttpResponseHandler {
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        onFailure(statusCode, headers, errorResponse==null?null:errorResponse.toString(), throwable);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        onFailure(statusCode, headers, errorResponse==null?null:errorResponse.toString(), throwable);
    }
}