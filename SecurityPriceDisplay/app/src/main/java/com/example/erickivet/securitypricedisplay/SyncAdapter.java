package com.example.erickivet.securitypricedisplay;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erickivet on 9/5/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    ContentResolver mContentResolver;
    public static final Uri CONTENT_URI = SecuritiesContract.Securities.CONTENT_URI;

    public SyncAdapter(Context context, boolean autoInit){
        super(context, autoInit);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInit, boolean allowParallelSyncs){
        super(context,autoInit,allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Starting Sync");
        getList();
    }

    private void getList(){
        Cursor cursor = mContentResolver.query(CONTENT_URI,null,null,null,null);
        while(cursor != null && cursor.moveToNext()){
            updateInfo(cursor.getString(cursor.getColumnIndex(SecuritiesContract.Securities.COLUMN_SECURITY_SYMBOL)));
        }
    }

    public void updateInfo(final String symbol){
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String stockUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol="+symbol;

        JsonObjectRequest jSonRequest = new JsonObjectRequest(Request.Method.GET, stockUrl,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try {
                    Log.d(TAG, "onResponse: Symbol: " + response.getString("LastPrice") +
                            " symbol: " + symbol);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("price", response.getString("LastPrice"));
                    mContentResolver.update(CONTENT_URI, contentValues,
                            SecuritiesContract.Securities.COLUMN_SECURITY_SYMBOL + "=?", new String[]{symbol});
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jSonRequest);
    }
}
