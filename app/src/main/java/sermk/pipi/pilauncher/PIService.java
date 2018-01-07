package sermk.pipi.pilauncher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.serenegiant.usb.USBMonitor;

import org.greenrobot.eventbus.EventBus;

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

    static public PIService getInstance(){
        return single;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:");
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.register();
        mUVCReciver = new UVCReciver(mUSBMonitor);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("PIService start!");
        single = this;
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

    private void setPositionHandler(){
        final boolean res = mUVCReciver.setCallBackPositionInRun(mBinder);
        Logger.v("set pos callback " + res);
    }

    private void unSetPositionHandler(){//todo may be not use?
        final boolean res = mUVCReciver.setCallBackPositionInRun(null);
        Logger.v("unset pos callback " + res);
    }

    private final PInterface_Impl mBinder = new PInterface_Impl();


    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "----onBind:" + intent);

        setPositionHandler();

        return mBinder;
    }

    @Override
    public void onRebind(final Intent intent) {
        Log.d(TAG, "onRebind:" + intent);
        setPositionHandler(); //todo may be not use?
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
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, LauncherAct.class), 0))  // The intent to send when the entry is clicked
            .build();

        startForeground(NOTIFICATION, notification);
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }

    public void startUVC(@Nullable final CVResolver.ICallbackPosition callback){
        if(mUVCReciver != null)
            if(!mUVCReciver.isAlive()) {
                mUVCReciver.startCapture(callback);
            }
    }

    public void completeUVC(){
        mUVCReciver.exitRun();
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Logger.v("onAttach");
            startUVC(mBinder);
            EventBus.getDefault().post(LauncherAct.State.STAND_TO);
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Logger.v("onConnect");
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Logger.v("onDisconnect");
        }
        @Override
        public void onDettach(final UsbDevice device) {
            Logger.v("onDettach");
            completeUVC();
            EventBus.getDefault().post(LauncherAct.State.REST);
        }

        @Override
        public void onCancel(final UsbDevice device) {
            Logger.v("onCancel");
        }

    };
}
