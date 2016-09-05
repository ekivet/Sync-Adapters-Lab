package com.example.erickivet.securitypricedisplay;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "MainActivity";
    private CursorAdapter mCursorAdapter;

    private TextView mTimeTextView;

    public static final String ACCOUNT_TYPE = "example.com";

    public static final String ACCOUNT = "default_account";

    public static final int LOADER_STOCKS = 0;

    private Account mAccount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccount = createSyncAccount(this);

        mCursorAdapter = new CursorAdapter(this,null,0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                if(cursor.getString(cursor.getColumnIndex(SecuritiesContract.Securities.COLUMN_EXCHANGE))
                        .equals("NASDAQ")){
                    view.setBackgroundColor(Color.GREEN);
                }else {
                    view.setBackgroundColor(Color.BLUE);
                }

                String name = cursor.getString(cursor.getColumnIndex(SecuritiesContract.Securities.COLUMN_SECURITYNAME));
                String symbol = cursor.getString(cursor.getColumnIndex(SecuritiesContract.Securities.COLUMN_SECURITY_SYMBOL));
                String quant = cursor.getString(cursor.getColumnIndex(SecuritiesContract.Securities.COLUMN_QUANT));

                text1.setText(name + "("+symbol+")");
                text2.setText(quant);
            }
        };

        ListView listView = (ListView)findViewById(R.id.watchlist);
        listView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(LOADER_STOCKS,null,this);

        mTimeTextView = (TextView) findViewById(R.id.updated_text);

        ContentResolver.setSyncAutomatically(mAccount,SecuritiesContract.AUTHORITY,true);
        ContentResolver.addPeriodicSync(
                mAccount,
                SecuritiesContract.AUTHORITY,
                Bundle.EMPTY,
                60);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case LOADER_STOCKS:
                return new CursorLoader(this,
                        SecuritiesContract.Securities.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: ");
        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,true);

        ContentResolver.requestSync(mAccount, SecuritiesContract.AUTHORITY,settingsBundle);
        mTimeTextView.setText("Last Updated: "+currentDateTime);
        mCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: ");
        mCursorAdapter.changeCursor(null);
    }

    public static Account createSyncAccount(Context context){

        Account newAccount = new Account(ACCOUNT,ACCOUNT_TYPE);

        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        if(accountManager.addAccountExplicitly(newAccount,null,null)){

        }else{

        }
        return newAccount;

    }
}
