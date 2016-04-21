package com.example.exprosic.spongebook2.booklist;

import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.utils.Debugging;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookListActivity extends AppCompatActivity {
    public static final String TAG = BookListActivity.class.getSimpleName();
    private static final String PARAM_USER_ID = "userId";

    @Bind(R.id.the_toolbar) Toolbar mToolBar;

    public static void startByUserId(Context context, int userId) {
        Intent intent = new Intent(context, BookListActivity.class);
        intent.putExtra(PARAM_USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int userId = getIntent().getIntExtra(PARAM_USER_ID, -1);
        Debugging.myAssert(userId>0, "userId is null");

        Fragment fragment = BookListFragment.newInstanceByUserId(userId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.the_fragment, fragment);
        transaction.commit();
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
