package sermk.pipi.game;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import sermk.pipi.pilib;

import com.orhanobut.logger.Logger;
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

    static private PIService single = null;
    private PiHandler pih = null;

    static public PIService getInstance(){
        return single;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:");
        if (mUVCReciver == null) {
            mUVCReciver = new UVCReciver(getApplicationContext());
        }
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("PIService start!");
        single = this;
        pih = new PiHandler();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");
        stopForeground(true/*removeNotification*/);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION);
            mNotificationManager = null;
        }
        single = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"@@@ onStartCommand" + intent.toString() + String.valueOf(flags) + String.valueOf(startId));
        return super.onStartCommand(intent, flags, startId);
    }

    private void setPositionHandler(){
        final boolean res = mUVCReciver.setCallBackPositionInRun(pih);
        Logger.v("set pos callback " + res);
    }

    private void unSetPositionHandler(){
        final boolean res = mUVCReciver.setCallBackPositionInRun(null);
        Logger.v("unset pos callback " + res);
    }

    //final PInterface_Impl pInterface_binder = new PInterface_Impl();
    final Pinterface.Stub pInterface_binder = new Pinterface.Stub() {
        @Override
        public int getPosition() throws RemoteException {
            return 0;
        }
    };

    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "----onBind:" + intent);

        setPositionHandler();

        //return mMessenger.getBinder();
        //return pih.getMessenger().getBinder();
        return pInterface_binder;
    }

    @Override
    public void onRebind(final Intent intent) {
        Log.d(TAG, "onRebind:" + intent);
        setPositionHandler();
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Log.d(TAG, "onUnbind:" + intent);
        unSetPositionHandler();
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
