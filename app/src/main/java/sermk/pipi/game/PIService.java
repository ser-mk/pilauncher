package sermk.pipi.game;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.serenegiant.usb.USBMonitor;

/**
 * Created by echormonov on 20.11.17.
 */

public final class PIService extends Service {
    private static final String TAG = "PIService";

    private static final int NOTIFICATION = R.string.app_name;

    private USBMonitor mUSBMonitor;
    private UVCReciver mUVCReciver;
    private NotificationManager mNotificationManager;

    public class LocalBinder extends Binder {
        PIService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PIService.this;
        }
    }
    // Binder given to clients
    private final IBinder mLocalBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:");
        if (mUVCReciver == null) {
            mUVCReciver = new UVCReciver(getApplicationContext());
        }
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("PIService start!");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");
        stopForeground(true/*removeNotification*/);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION);
            mNotificationManager = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "onBind:" + intent);
        return mLocalBinder;
    }

    @Override
    public void onRebind(final Intent intent) {
        Log.d(TAG, "onRebind:" + intent);
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Log.d(TAG, "onUnbind:" + intent);

        Log.d(TAG, "onUnbind:finished");
        return true;
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

    public void startUVC(final UVCReciver.Settings settings, @Nullable final CVResolver.ICallbackPosition callback){
        if(mUVCReciver != null)
            mUVCReciver.startCapture(settings, callback);
    }

    public void completeUVC(){
        mUVCReciver.exitRun();
    }
}
