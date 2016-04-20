package com.example.exprosic.spongebook2.book;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.scan.MultiscanActivity;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Created by exprosic on 4/14/2016.
 */
public class BookItem {
    public static final String NO_IMAGE = "noImage";
    public static final String IMAGE_LOADING = "imageLoading";

    public String mBookId;
    public String mTitle;
    public Map<String,String> mInfos;
    public String mImageUrl;

    public BookItem(String bookId, String title, Map<String,String> infos, String imageUrl) {
        mBookId = bookId;
        mTitle = title;
        mInfos = infos;
        mImageUrl = imageUrl;
    }

    public static BookItem getPlaceHolder() {
        return new BookItem(null, MyApplication.getInstance().getResources().getString(R.string.book_loading), null, IMAGE_LOADING);
    }

    public void setInvalid() {
        mBookId = null;
        mImageUrl = null;
        mTitle = MyApplication.getInstance().getResources().getString(R.string.book_search_failed);
    }

    public boolean isValid() {
        return mBookId != null;
    }
}
