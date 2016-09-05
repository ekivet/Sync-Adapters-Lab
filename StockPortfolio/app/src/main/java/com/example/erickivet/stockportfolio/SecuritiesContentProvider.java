package com.example.erickivet.stockportfolio;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;

/**
 * Created by erickivet on 9/3/16.
 */
public class SecuritiesContentProvider extends ContentProvider{

    private DBHelper myDB;
    private static final String AUTHORITY = SecuritiesContract.AUTHORITY;
    private static final String SECURITIES_TABLE = SecuritiesContract.Securities.TABLE_SECURITIES;
    public static final Uri CONTENT_URI = SecuritiesContract.Securities.CONTENT_URI;

    public static final int SECURITIES = 1;
    public static final int SECURITIES_ID = 2;
    public static final int SECURITIES_SYMBOLS = 3;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, SECURITIES_TABLE, SECURITIES);
        sUriMatcher.addURI(AUTHORITY, SECURITIES_TABLE + "/#", SECURITIES_ID);
        sUriMatcher.addURI(AUTHORITY, SECURITIES_TABLE + "/#", SECURITIES_SYMBOLS);
    }

    @Override
    public boolean onCreate() {
        myDB = DBHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);

        long id = 0;
        switch (uriType){
            case SECURITIES:
                id = myDB.addSecurity(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown UTI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(CONTENT_URI,id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int uriType = sUriMatcher.match(uri);
        Cursor cursor = null;

        switch (uriType){
            case SECURITIES_ID:
                break;
            case SECURITIES:
                cursor = myDB.getSecurities(selection);
                break;
            case SECURITIES_SYMBOLS:
                cursor = myDB.getSecuritySymbols(selection);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)){
            case SECURITIES:
                return SecuritiesContract.Securities.CONTENT_TYPE;
            case SECURITIES_ID:
                return SecuritiesContract.Securities.CONTENT_TYPE;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType){
            case SECURITIES:
                break;
            case SECURITIES_ID:
                String id = uri.getLastPathSegment();
                rowsDeleted = myDB.deleteSecurity(id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
