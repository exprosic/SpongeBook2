package com.example.exprosic.spongebook2.booklist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.example.exprosic.spongebook2.utils.Database;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

/**
 * Created by exprosic on 5/6/2016.
 */
public class RentInBookshelfItem extends BookshelfItem {
    public static final String TAG = RentInBookshelfItem.class.getSimpleName();
    private static final String FIELD_FROM_USER_ID = "fromUserId";
    private static final String FIELD_START_DATE_EPOCH_MS = "startTime";
    private static final String FIELD_DEPOSIT = "deposit";
    private static final String FIELD_RENTAL = "rental";

    private int mFromUserId;
    private Date mStartDate;
    private double mDeposit;
    private double mRental;

    public RentInBookshelfItem(int userId, String bookId, int fromUserId, Date startDate, double deposit, double rental) {
        super(userId, bookId);
        mFromUserId = fromUserId;
        mStartDate = startDate;
        mDeposit = deposit;
        mRental = rental;
    }

    public RentInBookshelfItem(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mFromUserId = jsonObject.getInt(FIELD_FROM_USER_ID);
        mStartDate = new Date(jsonObject.getLong(FIELD_START_DATE_EPOCH_MS));
        mDeposit = jsonObject.getDouble(FIELD_DEPOSIT);
        mRental = jsonObject.getDouble(FIELD_RENTAL);
    }

    public static abstract class DbContract implements BaseColumns {
        public static final String TABLE_NAME = "RentInBookshelfItemEntry";
        public static final String COLUMN_NAME_USER_ID = "UserId";
        public static final String COLUMN_NAME_BOOK_ID = "BookId";
        public static final String COLUMN_NAME_FROM_USER_ID = "FromUserId";
        public static final String COLUMN_NAME_START_DATE_EPOCH_MS = "StartDate";
        public static final String COLUMN_NAME_DEPOSIT = "Deposit";
        public static final String COLUMN_NAME_RENTAL = "Rental";

        public static final String SQL_CREATE_TABLE = String.format(Locale.US,
                "CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%3$s INTEGER, " +
                        "%4$s TEXT NOT NULL, " +
                        "%5$s INTEGER, " +
                        "%6$s INTEGER, " +
                        "%7$s REAL, " +
                        "%8$s REAL, " +
                        "UNIQUE(%3$s,%4$s));",
                TABLE_NAME, _ID, COLUMN_NAME_USER_ID, COLUMN_NAME_BOOK_ID, COLUMN_NAME_FROM_USER_ID,
                COLUMN_NAME_START_DATE_EPOCH_MS, COLUMN_NAME_DEPOSIT, COLUMN_NAME_RENTAL);

        public static final String SQL_DROP_TABLE = String.format(Locale.US,
                "DROP TABLE IF EXISTS %s;",
                TABLE_NAME);

        public static final String SQL_CLEAR_TABLE = String.format(Locale.US,
                "DELETE FROM %s",
                TABLE_NAME);
    }

    public static RentInBookshelfItem fromCursor(Cursor cursor) {
        return new RentInBookshelfItem(Database.getIntFromCursor(cursor, DbContract.COLUMN_NAME_USER_ID),
                Database.getStringFromCursor(cursor, DbContract.COLUMN_NAME_BOOK_ID),
                Database.getIntFromCursor(cursor, DbContract.COLUMN_NAME_FROM_USER_ID),
                new Date(Database.getLongFromCursor(cursor, DbContract.COLUMN_NAME_START_DATE_EPOCH_MS)),
                Database.getDoubleFromCursor(cursor, DbContract.COLUMN_NAME_DEPOSIT),
                Database.getDoubleFromCursor(cursor, DbContract.COLUMN_NAME_RENTAL));
    }

    @Override
    public void insertIntoDb(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DbContract.COLUMN_NAME_USER_ID, mUserId);
        values.put(DbContract.COLUMN_NAME_BOOK_ID, mBookId);
        values.put(DbContract.COLUMN_NAME_FROM_USER_ID, mFromUserId);
        values.put(DbContract.COLUMN_NAME_START_DATE_EPOCH_MS, mStartDate.getTime());
        values.put(DbContract.COLUMN_NAME_DEPOSIT, mDeposit);
        values.put(DbContract.COLUMN_NAME_RENTAL, mRental);
        db.insertWithOnConflict(DbContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
