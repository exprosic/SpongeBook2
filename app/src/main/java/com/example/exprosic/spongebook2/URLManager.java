package com.example.exprosic.spongebook2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by exprosic on 4/8/2016.
 */
public class URLManager {
    //public static String HOST = "http://127.0.0.1:8000/";
    public static String HOST = "http://123.57.56.221:8000/";

    private static String toHostUrl(String pattern, Object... args) {
        try {
            for (int i = 0; i < args.length; ++i)
                args[i] = URLEncoder.encode(args[i].toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Unsupported format 'utf-8'");
        }
        return HOST + String.format(pattern, args);
    }

    public static String bookInfoFromId(String bookId) {
        return toHostUrl("book/id/%s/", bookId);
    }

    public static String bookInfoFromIsbn(String isbn) {
        return toHostUrl("book/isbn/%s/", isbn);
    }
}
