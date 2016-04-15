package com.example.exprosic.spongebook2.book;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Created by exprosic on 4/14/2016.
 */
public class BookItem {
    private String mBookId;
    private String mTitle;
    private Map<String,String> mInfos;
    private String mImageUrl;

    public BookItem(String bookId, String title, Map<String,String> infos, String imageUrl) {
        mBookId = bookId;
        mTitle = title;
        mInfos = infos;
        mImageUrl = imageUrl;
    }

    public String getBookId() {
        return mBookId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public Map<String, String> getInfos() {
        return mInfos;
    }
}
