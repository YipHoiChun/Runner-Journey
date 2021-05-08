package com.example.runnerjourney;
import android.net.Uri;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;

public class JourneyProvider extends ContentProvider {
    DBHelper dbHelper;
    SQLiteDatabase database;

    private static final UriMatcher matcher;
    //Map URIs to codes so that we can decide which queries to make based on the URIs we receive.
    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "journey", 1);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "journey/#", 2);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "location", 3);
        matcher.addURI(JourneyProviderContract.AUTHORITY, "location/#", 4);
    }

    @Override
    public boolean onCreate() {
        Log.d("M", "The journey content provider has been created");
        dbHelper = new DBHelper(this.getContext());
        database = dbHelper.getWritableDatabase();
        return (database != null);
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return "vnd.android.cursor.dir/JourneyProvider.data.text";
        } else {
            return "vnd.android.cursor.item/JourneyProvider.data.text";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName;
        switch (matcher.match(uri)) {
            case 1:
                tableName = "journey";
                break;
            case 3:
                tableName = "location";
                break;
            default:
                tableName = "";
        }

        // Insert these values into the table and return the same URL, but with the ID appended.
        long id = database.insert(tableName, null, values);
        Uri uri1 = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(uri1, null);
        return uri1;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String selection, String[]
            selectionArgs, String sortOrder) {

        switch (matcher.match(uri)) {
            case 2:
                selection = "journeyID = " + uri.getLastPathSegment();
            case 1:
                return database.query("journey", strings, selection, selectionArgs, null, null, sortOrder);
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                return database.query("location", strings, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        String tN;//tableName
        int count;

        switch (matcher.match(uri)) {
            case 2:
                selection = "journeyID = " + uri.getLastPathSegment();
            case 1:
                tN = "journey";
                count = database.update(tN, values, selection, selectionArgs);
                break;
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                tN = "location";
                count = database.update(tN, values, selection, selectionArgs);
                break;
            default:
                return 0;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tN;//tableName
        int count;
        //given URI to table name
        switch (matcher.match(uri)) {
            case 2:
                selection = "journeyID = " + uri.getLastPathSegment();
            case 1:
                tN = "journey";
                count = database.delete(tN, selection, selectionArgs);
                break;
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                tN = "location";
                count = database.delete(tN, selection, selectionArgs);
                break;
            default:
                return 0;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
