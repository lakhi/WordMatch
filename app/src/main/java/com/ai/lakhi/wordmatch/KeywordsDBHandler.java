package com.ai.lakhi.wordmatch;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class KeywordsDBHandler {
    private static final String TAG = "KeywordsDatabase";
    public static final String COL_WORD = "WORD";
    private static final String DATABASE_NAME = "keywords";
    private static final int DATABASE_VERSION = 1;
    private final DatabaseOpenHelper mDatabaseOpenHelper;

    public KeywordsDBHandler(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + DATABASE_NAME +
                        " USING fts4(" +
                        COL_WORD + " TEXT)";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadKeywords();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
            onCreate(db);
        }

        private synchronized void loadKeywords() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private synchronized void loadWords() throws IOException {
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.keywords);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                if ((line = reader.readLine()) != null) {
                    String[] tokens = TextUtils.split(line, ", ");
                    for (String token:tokens) {
                        String keyword = token.replaceAll("\"", "");
                        long id = addWord(keyword);
                        Log.i(TAG, "Keyword '" + keyword + "' added to the database with an id: " + id);
                        if (id < 0) {
                            Log.e(TAG, "unable to add word: " + keyword);
                        }
                    }

                }
            } finally {
                reader.close();
            }
        }

        private synchronized long addWord(String word) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_WORD, word);

            return mDatabase.insert(DATABASE_NAME, null, initialValues);
        }

    }

    public List<String> getBestMatches(String searchText) {
        Cursor cursor = getWordMatches(searchText, null);
        List<String> bestMatches = new ArrayList<String>();

        if (returnValidCursor(cursor) == null)
            bestMatches = null;
        else if (cursor.moveToFirst()) {
            do {
                bestMatches.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return  bestMatches;
    }

    // The carat(^)query(*)asterisk ensures that keywords beginning with query are selected
    private Cursor getWordMatches(String query, String[] columns) {
        String selection = COL_WORD + " MATCH ?";
        String[] selectionArgs = new String[] {"^" + query + "*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(DATABASE_NAME);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        return returnValidCursor(cursor);
    }

    private Cursor returnValidCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /*
    To initiate creation of the database, because
    "The database is not actually created or opened until one of
    getWritableDatabase() or getReadableDatabase() is called"
    */
    public void getAllKeywords() {
        String selectQuery = "SELECT * FROM " + DATABASE_NAME;
        Cursor cursor = mDatabaseOpenHelper.getReadableDatabase().rawQuery(selectQuery, null);
    }
}
