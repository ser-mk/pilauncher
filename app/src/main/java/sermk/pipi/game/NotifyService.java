package sermk.pipi.game;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.serenegiant.common.BaseService;

import java.util.concurrent.TimeUnit;

/**
 * Created by echormonov on 18.11.17.
 */

final public class NotifyService extends Service {

    NotificationManager mNotificationManager;
    final String TAG = "NotifyService";
    private static final int NOTIFICATION = R.string.app_name;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.v("onCreate service");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification("@@@@@@@@@@@@@@");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.v("onStartCommand service");
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        Logger.v("onBind service");
        return null;
    }

    private void showNotification(final CharSequence text) {
        Log.v(TAG, "showNotification:" + text);
        // Set the info for the views that show in the notification panel.
        final Notification notification = new Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)  // the status icon
            .setTicker(text)  // the status text
            .setWhen(System.currentTimeMillis())  // the time stamp
            .setContentTitle(getText(R.string.app_name))  // the label of the entry
            .setContentText(text)  // the contents of the entry
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, Standalone.class), 0))  // The intent to send when the entry is clicked
            .build();

        startForeground(NOTIFICATION, notification);
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

}
