package com.example.exprosic.spongebook2.book;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.URLManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class BookInfoActivity extends Activity {
    private static String TAG = BookInfoActivity.class.getSimpleName();

    /* views */
    /* TODO Why can't be private? */
    @Bind(R.id.book_image) ImageView mBookImage;
    @Bind(R.id.book_infos) LinearLayout mBookInfos;

    /* params */
    private static String PARAM_BOOKID ="bookId";
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);
        ButterKnife.bind(this);

        dumpInfoFromIntent(getIntent());
        fetchBookInfo(bookId);
    }

    private void dumpInfoFromIntent(Intent intent) {
        bookId = intent.getStringExtra(PARAM_BOOKID);
        assert bookId != null;
    }

    public static void startWithBookId(Context context, String bookId) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        intent.putExtra(PARAM_BOOKID, bookId);
        context.startActivity(intent);
    }

    public void fetchBookInfo(final String bookId) {
        assert bookId != null;
        MyApplication.getAuthorizedClient().get(this, URLManager.bookInfoFromId(bookId),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] headers, JSONObject jsonBook) {
                        try {
                            String title = jsonBook.getString("title");

                            JSONObject jsonInfos = jsonBook.getJSONObject("infos");
                            Map<String, String> infos = new HashMap<>(jsonInfos.length());
                            for (Iterator<String> iterator=jsonInfos.keys(); iterator.hasNext();) {
                                String key=iterator.next();
                                infos.put(key, jsonInfos.getString(key));
                            }

                            String imageUrl = null;
                            try {
                                imageUrl = jsonBook.getString("imageurl");
                            } catch (JSONException e) {
                                /* no image, remain null */
                            }

                            renderBook(bookId, title, infos, imageUrl);
                        } catch (JSONException e) {
                            Log.e(TAG, "Bad Json Book Format: "+jsonBook.toString());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int status, Header[] headers, String response, Throwable throwable) {
                        Toast.makeText(BookInfoActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(BookInfoActivity.this, "Request Failed (JSONObject)", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void renderBook(String bookId, String title, Map<String,String> infos, String imageUrl) {
        mBookInfos.removeAllViews();

        /* set title */
        TextView titleTextView = new TextView(this);
        titleTextView.setText(title);
        mBookInfos.addView(titleTextView);

        /* set infos */
        for (Map.Entry<String,String> entry: infos.entrySet()) {
            TextView entryTextView = new TextView(this);
            entryTextView.setText(entry.getKey()+": "+entry.getValue());
        }

        /* set image if any */
        if (imageUrl != null) {
            Picasso.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.waiting_book_image)
                    .into(mBookImage);
        }
    }
}
