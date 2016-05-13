package com.example.exprosic.spongebook2.book;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.booklist.BookListProvider;
import com.example.exprosic.spongebook2.booklist.BorrowableBookshelfItem;
import com.example.exprosic.spongebook2.utils.Debugging;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class RentSettingActivity extends AppCompatActivity {
    public static final String TAG = RentSettingActivity.class.getSimpleName();
    public static final String PARAM_NEW_ITEM = "newItem";

    private static final String PARAM_BOOKSHELF_ITEM = "bookshelfItem";

    @Bind(R.id.the_toolbar)
    Toolbar mToolBar;
    @Bind(R.id.check_borrowable)
    CheckBox mCheckBorrowable;
    @Bind(R.id.edit_deposit)
    EditText mEditDeposit;
    @Bind(R.id.edit_rental)
    EditText mEditRental;
    @Bind(R.id.button_submit)
    Button mButtonSubmit;

    private BorrowableBookshelfItem mBookshelfItem;

    public static void startWithBookshelfItem(Activity activity, BorrowableBookshelfItem bookshelfItem, int resultCode) {
        Intent intent = new Intent(activity, RentSettingActivity.class);
        intent.putExtra(PARAM_BOOKSHELF_ITEM, bookshelfItem);
        activity.startActivityForResult(intent, resultCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_setting);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dumpInfoFromIntent(getIntent());
        render();
    }

    private void dumpInfoFromIntent(Intent intent) {
        mBookshelfItem = (BorrowableBookshelfItem) intent.getSerializableExtra(PARAM_BOOKSHELF_ITEM);
    }

    private void render() {
        mEditDeposit.setText(String.format(Locale.US, "%.2f", mBookshelfItem.getDeposit()));
        mEditRental.setText(String.format(Locale.US, "%.2f", mBookshelfItem.getRental()));
        mCheckBorrowable.setChecked(mBookshelfItem.isBorrowable());
        mEditDeposit.setEnabled(mBookshelfItem.isBorrowable());
        mEditRental.setEnabled(mBookshelfItem.isBorrowable());
    }

    @OnCheckedChanged(R.id.check_borrowable)
    void borrowableChanged(boolean checked) {
        mEditDeposit.setEnabled(checked);
        mEditRental.setEnabled(checked);
    }

    @OnClick(R.id.button_submit)
    void submit() {
        try {
            boolean borrowable = mCheckBorrowable.isChecked();
            double deposit = Double.parseDouble(mEditDeposit.getText().toString());
            double rental = Double.parseDouble(mEditRental.getText().toString());
            mBookshelfItem.setBorrowable(borrowable);
            mBookshelfItem.setDeposit(deposit);
            mBookshelfItem.setRental(rental);
            mButtonSubmit.setEnabled(false);
            mButtonSubmit.setText(R.string.loading);
            MyApplication.getBookListProvider().updateBookshelfItem(this, mBookshelfItem, new BookListProvider.onBookshelfItemUpdatedListener() {
                @Override
                public void onBookshelfItemUpdated(boolean succeeded) {
                    if (succeeded) {
                        Debugging.makeRawToast(RentSettingActivity.this, Toast.LENGTH_SHORT, "请求成功");
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(PARAM_NEW_ITEM, mBookshelfItem);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Debugging.makeRawToast(RentSettingActivity.this, Toast.LENGTH_SHORT, "请求失败");
                        mButtonSubmit.setText(R.string.submit);
                        mButtonSubmit.setEnabled(true);
                    }
                }
            });
        } catch (NumberFormatException e) {
            Debugging.makeRawToast(this, Toast.LENGTH_SHORT, "数字格式错误");
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
