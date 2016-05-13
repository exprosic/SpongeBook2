package com.example.exprosic.spongebook2.booklist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.example.exprosic.spongebook2.utils.Database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by exprosic on 5/6/2016.
 */
public class BorrowableBookshelfItem extends BookshelfItem {
    public static final String TAG = BorrowableBookshelfItem.class.getSimpleName();

    protected boolean mBorrowable;
    protected double mDeposit;
    protected double mRental;

    public BorrowableBookshelfItem(int userId, String bookId, boolean borrowable, double deposit, double rental) {
        super(userId, bookId);
        mBorrowable = borrowable;
        mDeposit = deposit;
        mRental = rental;
    }

    // new book with only userId & bookId
    public BorrowableBookshelfItem(int userId, String bookId) {
        super(userId, bookId);
        mBorrowable = false;
        mDeposit = 0.;
        mRental = 0.;
    }

    public boolean isBorrowable() {
        return mBorrowable;
    }

    public void setBorrowable(boolean borrowable) {
        mBorrowable = borrowable;
    }

    public double getDeposit() {
        return mDeposit;
    }

    public void setDeposit(double deposit) {
        mDeposit = deposit;
    }

    public double getRental() {
        return mRental;
    }

    public void setRental(double rental) {
        mRental = rental;
    }

    protected static final String FIELD_BORROWABLE = "borrowable";
    protected static final String FIELD_DEPOSIT = "deposit";
    protected static final String FIELD_RENTAL = "rental";

    public BorrowableBookshelfItem(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mBorrowable = jsonObject.getBoolean(FIELD_BORROWABLE);
        mDeposit = jsonObject.getDouble(FIELD_DEPOSIT);
        mRental = jsonObject.getDouble(FIELD_RENTAL);
    }

    public static List<BookshelfItem> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<BookshelfItem> borrowableBookshelfItems = new ArrayList<>(jsonArray.length());
        for (int i=0; i<jsonArray.length(); ++i)
            borrowableBookshelfItems.add(new BorrowableBookshelfItem(jsonArray.getJSONObject(i)));
        return borrowableBookshelfItems;
    }

    public static abstract class DbContract implements BaseColumns {
        public static final String TABLE_NAME = "BorrowableBookshelfItemEntry";
        public static final String COLUMN_NAME_USER_ID = "UserId";
        public static final String COLUMN_NAME_BOOK_ID = "BookId";
        public static final String COLUMN_NAME_BORROWABLE = "Borrowable";
        public static final String COLUMN_NAME_DEPOSIT = "Deposit";
        public static final String COLUMN_NAME_RENTAL = "Rental";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%3$s INTEGER, " +
                        "%4$s TEXT NOT NULL, " +
                        "%5$s INTEGER, " +
                        "%6$s REAL, " +
                        "%7$s REAL, " +
                        "UNIQUE(%3$s,%4$s));",
                TABLE_NAME, _ID, COLUMN_NAME_USER_ID, COLUMN_NAME_BOOK_ID,
                COLUMN_NAME_BORROWABLE, COLUMN_NAME_DEPOSIT, COLUMN_NAME_RENTAL);

        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE IF EXISTS %s;",
                TABLE_NAME);

        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s",
                TABLE_NAME);
    }

    public static BorrowableBookshelfItem fromCursor(Cursor cursor) {
        return new BorrowableBookshelfItem(Database.getIntFromCursor(cursor, DbContract.COLUMN_NAME_USER_ID),
                Database.getStringFromCursor(cursor, DbContract.COLUMN_NAME_BOOK_ID),
                Database.getIntFromCursor(cursor, DbContract.COLUMN_NAME_BORROWABLE)>0,
                Database.getDoubleFromCursor(cursor, DbContract.COLUMN_NAME_DEPOSIT),
                Database.getDoubleFromCursor(cursor, DbContract.COLUMN_NAME_RENTAL));
    }

    @Override
    public void insertIntoDb(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DbContract.COLUMN_NAME_USER_ID, mUserId);
        values.put(DbContract.COLUMN_NAME_BOOK_ID, mBookId);
        values.put(DbContract.COLUMN_NAME_BORROWABLE, mBorrowable?1:0);
        values.put(DbContract.COLUMN_NAME_DEPOSIT, mDeposit);
        values.put(DbContract.COLUMN_NAME_RENTAL, mRental);
        db.insertWithOnConflict(DbContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
