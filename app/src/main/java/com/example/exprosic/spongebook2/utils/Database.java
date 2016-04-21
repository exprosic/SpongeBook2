package com.example.exprosic.spongebook2.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by exprosic on 4/15/2016.
 */
public class Database {
    public static Cursor rawQuery(SQLiteDatabase db, String sql, Object... args) {
        String[] strArgs = new String[args.length];
        for (int i=0; i<args.length; ++i)
            strArgs[i] = args[i].toString();
        return db.rawQuery(sql, strArgs);
    }

    public static int getIntFromCursor(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static String getStringFromCursor(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }
}
