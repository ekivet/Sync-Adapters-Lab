package com.example.erickivet.stockportfolio;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final Uri CONTENT_URI = SecuritiesContract.Securities.CONTENT_URI;
    ListView listview;
    CursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listview = (ListView) findViewById(R.id.watchlist);

        Cursor cursor = getContentResolver().query(CONTENT_URI, null,null,null,null);
        mCursorAdapter = new CursorAdapter(this, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2,
                        parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                String name = cursor.getString(cursor.getColumnIndex("securityname"));
                String symbol = cursor.getString(cursor.getColumnIndex("symbol"));
                String quant = cursor.getString(cursor.getColumnIndex("quant"));

                text1.setText(name+" ("+symbol+") ");
                text2.setText("Shares: "+quant);
            }
        };

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                getContentResolver().delete(ContentUris.withAppendedId(CONTENT_URI,id),null,null);
                return false;
            }
        });

        listview.setAdapter(mCursorAdapter);

        getContentResolver().registerContentObserver(CONTENT_URI,true,new Observer(new Handler()));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
            }
        });
    }

    public void retrieveSecurity(final String symbol, final String quant){

        RequestQueue queue = Volley.newRequestQueue(this);
        String securityUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol="+symbol;

        JsonObjectRequest securityJsonRequest = new JsonObjectRequest
                (Request.Method.GET,securityUrl,null,new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MainActivity.class.getName(),"Response: "+ response.toString());
                        try{
                            if(response.has("Status") && response.getString("Status").equals("SUCCESS")){
                                retrieveExchange(symbol,quant,response.getString("Name"));
                            }else{
                                Toast.makeText(MainActivity.this,"The Security you Entered is Invalid"
                                ,Toast.LENGTH_LONG).show();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(MainActivity.class.getName(),error.toString());
                    }
                });
        queue.add(securityJsonRequest);

    }

    public void retrieveExchange (final String symbol, final String quant, final String name){

        RequestQueue queue = Volley.newRequestQueue(this);
        String exchangeUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input="+symbol;

        Log.d(MainActivity.class.getName(), "Starting exchange request: "+exchangeUrl);
        JsonArrayRequest exchangeJsonRequest = new JsonArrayRequest
                (exchangeUrl, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(MainActivity.class.getName(), "Response2: " + response.toString());
                        try {
                            ContentResolver contentResolver = getContentResolver();
                            String exchange = ((JSONObject) response.get(0)).getString("Exchange");
                            ContentValues values = new ContentValues();
                            values.put("symbol", symbol);
                            values.put("quant", quant);
                            values.put("securityname", name);
                            values.put("exchange", exchange);
                            contentResolver.insert(CONTENT_URI, values);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(MainActivity.class.getName(),"Error Occured");
                    }
                });
        queue.add(exchangeJsonRequest);
    }

    private void createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.add_security_dialog,null))
                .setPositiveButton("Add", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Boolean isValid = true;
                EditText symbolText = (EditText)((AlertDialog)dialog).findViewById(R.id.add_symbol);
                EditText quantText = (EditText)((AlertDialog)dialog).findViewById(R.id.share_quant);
                if(symbolText.getText().toString().length() == 0 || quantText.getText().toString().length() == 0){
                    Toast.makeText(MainActivity.this,"You must complete all fields", Toast.LENGTH_LONG).show();
                    isValid = false;
                }else {
                    symbolText.setError("");
                }

                if(isValid){
                    retrieveSecurity(symbolText.getText().toString().toUpperCase(),quantText.getText().toString());
                }
            }
        }).setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class Observer extends ContentObserver {
        public Observer(Handler handler){
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            new AsyncTask<Void, Void, Cursor>(){
                @Override
                protected Cursor doInBackground(Void... params) {
                    return getContentResolver().query(CONTENT_URI,null,null,null,null);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    super.onPostExecute(cursor);
                    mCursorAdapter.swapCursor(cursor);
                }
            }.execute();
        }
    }


}
