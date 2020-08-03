package com.microchip.android.mcp2221terminal;

import android.provider.BaseColumns;

import junit.framework.Test;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DB {
    public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DB() {
    }

    /* Inner class that defines the table contents */
    public static class TestEntry implements BaseColumns {
        public static final String TABLE_NAME = "Test";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DATE = "Date";
        public static final String COLUMN_NAME_T1 = "T1";
        public static final String COLUMN_NAME_T2 = "T2";
    }

    public static class TestNameEntry implements BaseColumns {
        public static final String TABLE_NAME = "TestName";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DATE = "Date";
    }

    public static final String SQL_CREATE_ENTRIES_Test =
            "CREATE TABLE IF NOT EXISTS " + TestEntry.TABLE_NAME + " (" +
                    TestEntry._ID + " INTEGER PRIMARY KEY," +
                    TestEntry.COLUMN_NAME_TITLE + " TEXT," +
                    TestEntry.COLUMN_NAME_DATE + " TEXT," +
                    TestEntry.COLUMN_NAME_T1 + " REAL," +
                    TestEntry.COLUMN_NAME_T2 + " REAL)";

    public static final String SQL_DELETE_ENTRIES_Test =
            "DROP TABLE IF EXISTS " + TestEntry.TABLE_NAME;

    public static final String SQL_CREATE_ENTRIES_TestName =
            "CREATE TABLE IF NOT EXISTS " + TestNameEntry.TABLE_NAME + " (" +
                    TestNameEntry._ID + " INTEGER PRIMARY KEY," +
                    TestNameEntry.COLUMN_NAME_TITLE + " TEXT UNIQUE," +
                    TestNameEntry.COLUMN_NAME_DATE + " TEXT)";

    public static final String SQL_DELETE_ENTRIES_TestName =
            "DROP TABLE IF EXISTS " + TestNameEntry.TABLE_NAME;

    public static String insert(String title, String date, double t1, double t2) {
        return "INSERT INTO " + TestEntry.TABLE_NAME
                + " (" + TestEntry.COLUMN_NAME_TITLE + " ," + TestEntry.COLUMN_NAME_DATE + " ,"
                + TestEntry.COLUMN_NAME_T1 + " ," + TestEntry.COLUMN_NAME_T2 + ") " +
                "VALUES(" + title + ", " + date + " ," + t1 + " ," + t2 + ")";
    }
    public static String insert_now(String title, double t1, double t2){
        return "INSERT INTO " + TestEntry.TABLE_NAME
                + " (" + TestEntry.COLUMN_NAME_TITLE + " ," + TestEntry.COLUMN_NAME_DATE + " ,"
                + TestEntry.COLUMN_NAME_T1 + " ," + TestEntry.COLUMN_NAME_T2 + ") " +
                "VALUES(" + title + ", " + formatter.format(new Date(System.currentTimeMillis())) + " ," + t1 + " ," + t2 + ")";
    }
}
