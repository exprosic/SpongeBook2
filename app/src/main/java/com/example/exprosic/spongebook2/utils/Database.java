package com.example.exprosic.spongebook2.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by exprosic on 4/15/2016.
 */
public class Database {
    public static Cursor rawQuery(SQLiteDatabase db, String sql, Object... args) {
        return db.rawQuery(sql, StringUtils.toStrings(args));
    }

    public static int getIntFromCursor(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static long getLongFromCursor(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    public static String getStringFromCursor(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public static double getDoubleFromCursor(Cursor cursor, String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    public static QueryBuilder select(SQLiteDatabase db, String tableName) {
        return new QueryBuilder(db).table(tableName);
    }

    public static class QueryBuilder {
        private SQLiteDatabase mDb;
        private String mTableName;
        private boolean mDistinct = false;
        private String[] mColumns;
        private String mSelection;
        private String[] mSelectionArgs;
        private String mGroupBy;
        private String mHaving;
        private String mOrderBy;
        private String mLimit;

        QueryBuilder(SQLiteDatabase db) {
            mDb = db;
        }

        public QueryBuilder table(String tableName) {
            mTableName = tableName;
            return this;
        }

        public QueryBuilder distinct() {
            mDistinct = true;
            return this;
        }

        public QueryBuilder columns(String... args) {
            mColumns = args;
            return this;
        }

        public QueryBuilder where(String selection, Object... args) {
            mSelection = selection;
            mSelectionArgs = StringUtils.toStrings(args);
            return this;
        }

        public QueryBuilder groupBy(String groupBy) {
            mGroupBy = groupBy;
            return this;
        }

        public QueryBuilder having(String having) {
            mHaving = having;
            return this;
        }

        public QueryBuilder orderBy(String orderBy) {
            mOrderBy = orderBy;
            return this;
        }

        public QueryBuilder limit(String limit) {
            mLimit = limit;
            return this;
        }

        public Cursor query() {
            return mDb.query(mDistinct, mTableName, mColumns, mSelection, mSelectionArgs,
                    mGroupBy, mHaving, mOrderBy, mLimit);
        }
    }
}