package com.example.erickivet.stockportfolio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by erickivet on 9/3/16.
 */
public class DBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "watchlistDB.db";
    private static final String TABLE_SECURITIES = SecuritiesContract.Securities.TABLE_SECURITIES;

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_SECURITY_SYMBOL = SecuritiesContract.Securities.COLUMN_SECURITY_SYMBOL;
    public static final String COLUMN_SECURITYNAME = SecuritiesContract.Securities.COLUMN_SECURITYNAME;
    public static final String COLUMN_QUANT = SecuritiesContract.Securities.COLUMN_QUANT;
    public static final String COLUMN_EXCHANGE = SecuritiesContract.Securities.COLUMN_EXCHANGE;

    private static DBHelper dbInstance;

    public static DBHelper getInstance(Context context){
        if(dbInstance == null){
            dbInstance = new DBHelper(context);
        }
        return dbInstance;
    }

    private DBHelper (Context context){
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " +
                TABLE_SECURITIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SECURITYNAME + " TEXT,"
                + COLUMN_EXCHANGE + " TEXT,"
                + COLUMN_SECURITY_SYMBOL + " TEXT,"
                + COLUMN_QUANT + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITIES);
        onCreate(db);
    }

    public long addSecurity(ContentValues values){
        SQLiteDatabase db = getWritableDatabase();
        long insertedRow = db.insert(TABLE_SECURITIES,null,values);
        db.close();
        return insertedRow;
    }

    public Cursor getSecuritySymbols(String selection){
        String [] projection = {COLUMN_SECURITY_SYMBOL};

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SECURITIES,projection,selection,null,null,null,null);
        return cursor;
    }

    public Cursor getSecurities(String selection){
        String[] projection = {COLUMN_ID,COLUMN_SECURITY_SYMBOL,COLUMN_SECURITYNAME,COLUMN_EXCHANGE,
        COLUMN_QUANT};

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SECURITIES,projection,selection,null,null,null,null);

        return cursor;
    }

    public int deleteSecurity(String id){
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(TABLE_SECURITIES,COLUMN_ID+"=?",new String[]{id});
        db.close();
        return rowsDeleted;
    }
}
