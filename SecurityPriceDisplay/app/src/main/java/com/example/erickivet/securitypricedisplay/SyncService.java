package com.example.erickivet.securitypricedisplay;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by erickivet on 9/5/16.
 */
public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;



    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if(sSyncAdapter == null)
                sSyncAdapter = new SyncAdapter(getApplicationContext(),true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
