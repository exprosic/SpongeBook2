package com.example.exprosic.spongebook2.test;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MainActivity;
import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.book.BookInfoActivity;
import com.example.exprosic.spongebook2.scan.MultiscanActivity;

public class TestEntryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_entry);
//        BookInfoActivity.startWithBookId(this, "jd,10592815");

//        MultiscanActivity.start(this);
        startActivity(new Intent(this, MainActivity.class));
    }
}
