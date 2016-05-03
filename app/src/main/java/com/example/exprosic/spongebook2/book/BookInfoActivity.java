package com.example.exprosic.spongebook2.book;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.utils.Debugging;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookInfoActivity extends AppCompatActivity {
    private static String TAG = BookInfoActivity.class.getSimpleName();

    /* views */
    @Bind(R.id.the_toolbar)
    Toolbar mToolBar;
    @Bind(R.id.book_image)
    ImageView mBookImage;
    @Bind(R.id.book_infos)
    LinearLayout mBookInfos;
    @Bind(R.id.button_borrow)
    Button mButtonBorrow;

    /* params */
    private static final String PARAM_BOOKID = "bookId";
    private String mBookId;
    private static final String PARAM_USERID = "userId";
    private int mUserId; // -1 for not provided

    public static void start(Context context, String bookId) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        intent.putExtra(PARAM_BOOKID, bookId);
        context.startActivity(intent);
    }

    public static void startWithUser(Context context, String bookId, int userId) {
        Intent intent = new Intent(context, BookInfoActivity.class);
        intent.putExtra(PARAM_BOOKID, bookId);
        intent.putExtra(PARAM_USERID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dumpInfoFromIntent(getIntent());
        fetchBookInfo(mBookId);
    }

    private void dumpInfoFromIntent(Intent intent) {
        mBookId = intent.getStringExtra(PARAM_BOOKID);
        mUserId = intent.getIntExtra(PARAM_USERID, -1);
        assert mBookId != null;

        if (mUserId != -1) {
            mButtonBorrow.setVisibility(View.VISIBLE);
            if (MyApplication.isMyself(mUserId)) {
                mButtonBorrow.setText(R.string.borrow_setting);
                mButtonBorrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: setting
                        Debugging.makeRawToast(BookInfoActivity.this, Toast.LENGTH_SHORT, "setting");
                    }
                });
            } else {
                if ()
                mButtonBorrow.setText(R.string.borrow_book);
                mButtonBorrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: borrow
                        Debugging.makeRawToast(BookInfoActivity.this, Toast.LENGTH_SHORT, "borrow");
                    }
                });
            }
        }
    }

    public void fetchBookInfo(final String bookId) {
        assert bookId != null;
        MyApplication.getBookProvider().fetchBookById(this, bookId, new BookProvider.OnFetchedListener() {
            @Override
            public void onBookFetched(final BookItem bookItem) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        renderBook(bookItem);
                    }
                });
            }
        });
    }

    private void renderBook(BookItem bookItem) {
        mBookInfos.removeAllViews();

        /* set title */
        TextView titleTextView = new TextView(this);
        titleTextView.setText(bookItem.mTitle);
        mBookInfos.addView(titleTextView);

        /* set infos */
        for (Map.Entry<String, String> entry : bookItem.mInfos.entrySet()) {
            TextView entryTextView = new TextView(this);
            entryTextView.setText(String.format(Locale.CHINESE, "%s: %s", entry.getKey(), entry.getValue()));
            mBookInfos.addView(entryTextView);
        }

        /* set image if any */
        if (bookItem.mImageUrl != null) {
            Picasso.with(this)
                    .load(bookItem.mImageUrl)
                    .placeholder(R.drawable.waiting_book_image)
                    .into(mBookImage);
        } else {
            mBookImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
