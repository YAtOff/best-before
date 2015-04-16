package com.example.yavor.bestbefore.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.yavor.bestbefore.MainActivity;
import com.example.yavor.bestbefore.R;
import com.example.yavor.bestbefore.data.ProductContract.ProductEntry;

public class ExpireService extends IntentService {

    public static final int EXPIRE_NOTIFICATION_ID = 1;

    public ExpireService() {
        super("Expire");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (hasExpiredProducts()) {
            notifyForExpiredProducts();
        }
    }

    private boolean hasExpiredProducts() {
        long bestBefore = System.currentTimeMillis() / 1000 + 60 * 60 * 24;
        Cursor cursor = getContentResolver().query(
                ProductEntry.buildProductUriWithBestBefore(bestBefore),
                null,
                null,
                null,
                null
        );
        boolean result = 0 < cursor.getCount();
        cursor.close();
        return result;
    }

    private void notifyForExpiredProducts() {
        // NotificationCompatBuilder is a very convenient way to build backward-compatible
        // notifications.  Just throw in some data.
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Expired products")
                        .setContentText("There are products that will expire soon.");

        // Make something interesting happen when the user clicks on the notification.
        // In this case, opening the app is sufficient.
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(EXPIRE_NOTIFICATION_ID, notificationBuilder.build());
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent expireCheckIntent = new Intent(context, ExpireService.class);
            context.startService(expireCheckIntent);
        }
    }
}
