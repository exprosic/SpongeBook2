package com.example.exprosic.spongebook2;

import com.example.exprosic.spongebook2.utils.ListUtils;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by exprosic on 4/8/2016.
 */
public class URLManager {
    public static String HOST = "http://10.0.65.19:8000/";
//    public static String HOST = "http://123.57.56.221:8000/";

    public static String bookInfoFromId(String bookId) {
        return toHostUrl("book/id/%s/", bookId);
    }
    public static String bookInfoFromIsbn(String isbn) {
        return toHostUrl("book/isbn/%s/", isbn);
    }
    public static class BookList {
        public static String get(int userId) {
            return toHostUrl("user/%s/bookshelf/", userId);
        }
        public static class post {
            public static String URL = toHostUrl("bookshelf/");
            public static RequestParams params(List<String> bookIds) {
                return new RequestParamBuilder().append("bookIds", ListUtils.concatenate(bookIds)).done();
            }
        }
    }

    public static class login {
        public static String URL = toHostUrl("login/");
        public static RequestParams params(String username, String password) {
            return new RequestParamBuilder().append("username", username).append("password", password).done();
        }
    }

    public static String logout = toHostUrl("logout/");

    private static String toHostUrl(String pattern, Object... args) {
        try {
            for (int i = 0; i < args.length; ++i)
                args[i] = URLEncoder.encode(args[i].toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Unsupported format 'utf-8'");
        }
        return HOST + String.format(pattern, args);
    }

    private static class RequestParamBuilder {
        private RequestParams params = new RequestParams();
        public RequestParamBuilder append(String key, String val) {
            params.add(key, val);
            return this;
        }
        public RequestParams done() {
            return params;
        }
    }
}
