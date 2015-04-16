package com.example.yavor.bestbefore.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BestbeforeSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static BestbeforeSyncAdapter sBestbeforeSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("BestbeforeSyncService", "onCreate - BestbeforeSyncService");
        synchronized (sSyncAdapterLock) {
            if (sBestbeforeSyncAdapter == null) {
                sBestbeforeSyncAdapter = new BestbeforeSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sBestbeforeSyncAdapter.getSyncAdapterBinder();
    }
}