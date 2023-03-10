package sermk.pipi.pilauncher;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.serenegiant.usb.USBMonitor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import sermk.pipi.pilauncher.GUIFragment.CVMaskResolver;
import sermk.pipi.pilauncher.externalcooperation.PiSettings;

/**
 * Created by echormonov on 20.11.17.
 */

public final class PIService extends Service {
    private static final String TAG = "PIService";

    private static final int NOTIFICATION = R.string.app_name;

    private USBMonitor mUSBMonitor;
    private UVCReciver mUVCReciver;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate:");
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUSBMonitor.register();
        mUVCReciver = new UVCReciver(mUSBMonitor);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification("PIService start!");
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean stopService(Intent name) {
        EventBus.getDefault().unregister(this);
        return super.stopService(name);
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

    private void setPositionHandler(){
        final boolean res = mUVCReciver.setCallBackPositionInRun(mBinder);
        Logger.i("set pos callback " + res);
    }

    private void releasePositionHandler(){//todo may be not use?
        final boolean res = mUVCReciver.setCallBackPositionInRun(null);
        Logger.i("unset pos callback " + res);
    }

    private final PInterface_Impl mBinder = new PInterface_Impl();


    @Override
    public IBinder onBind(final Intent intent) {
        Log.i(TAG, "----onBind:" + intent);

        setPositionHandler();

        return mBinder;
    }

    @Override
    public void onRebind(final Intent intent) {
        Log.i(TAG, "onRebind:" + intent);
        setPositionHandler(); //todo may be not use?
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Log.i(TAG, "onUnbind:" + intent);
        releasePositionHandler();
        Log.i(TAG, "onUnbind:finished");
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

    public static void startUVCwithCallbackPosition(CVResolver.ICallbackPosition callback){
        EventBus.getDefault().post(callback);
    }

    private enum RESTART_UVC { WAIT_RESTART }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void restartUVC(RESTART_UVC restart){
        int try_restart = PiSettings.getInstance().getCurrentSettings()
            .behaviorSettings.TIMES_TRY_RESTART_UVC;
        while (mUVCReciver.isAlive() && try_restart != 0){
            mUVCReciver.exitRun();
            try_restart--;
            Log.w(TAG, "try exit UVC Thread!");
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(try_restart == 0)
            LauncherAct.restartThisApp();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void startUVC(CVResolver.ICallbackPosition callback){
        mBinder.clearPosition();
        if(mUVCReciver != null)
            if(!mUVCReciver.isAlive()) {
                mUVCReciver.startCapture(callback);
            } else { //todo forse release if run!
                Log.w(TAG, "thread UVC run!");
                EventBus.getDefault().post(RESTART_UVC.WAIT_RESTART);
            }

    }

    private enum SIGNAL_VIBRATION {SIGNAL}

    public static void external_trySignalVibration(){
        EventBus.getDefault().post(SIGNAL_VIBRATION.SIGNAL);
    }

    @Subscribe
    public void trySignalVibration(SIGNAL_VIBRATION signal){
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        final long time = PiSettings.getInstance().getCurrentSettings().behaviorSettings.VIBRATE_TIME_MS;
        if(v.hasVibrator()) {
            Log.v(TAG, "hasVibrator");
            v.vibrate(time);
        }
    }

    private enum STOP_UVC_TYPE {STOP}

    public static void external_completeUVC(){
        EventBus.getDefault().post(STOP_UVC_TYPE.STOP);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void completeUVC(STOP_UVC_TYPE stop) {
            mUVCReciver.exitRun();
            mBinder.sendingCloseCode();
    }

    public enum ATTACHMENT_USB_INFO {ATTACHED, DETACHED}

    public enum CONNECTED_USB_INFO {CONNECTED, DISCONNECTED}

    private static ATTACHMENT_USB_INFO statusAttachedUSB = ATTACHMENT_USB_INFO.DETACHED;

    private static CONNECTED_USB_INFO statusConnectedUSB = CONNECTED_USB_INFO.DISCONNECTED;

    public static ATTACHMENT_USB_INFO getStatusAttachedUSB(){return statusAttachedUSB;}

    public static CONNECTED_USB_INFO getStatusConnectedUSB(){return statusConnectedUSB;}

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Logger.i("onAttach");
            startUVC(mBinder);
            LauncherAct.lightOn();
            statusAttachedUSB = ATTACHMENT_USB_INFO.ATTACHED;
            EventBus.getDefault().post(statusAttachedUSB);
        }

        //todo: remove run game, set state connected
        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Logger.i("onConnect");
            //LauncherAct.tryStartGame();
            statusConnectedUSB = CONNECTED_USB_INFO.CONNECTED;
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Logger.i("onDisconnect");

            statusConnectedUSB = CONNECTED_USB_INFO.DISCONNECTED;

            final long refFrame = mUVCReciver.getRefCaptureFrameMat();
            if(refFrame == 0)
                return;
            final byte[] capture = CVMaskResolver.convertMat2BAGRAY(refFrame);
            if(PiSettings.getInstance().confirmSettings(
                PIService.this,capture)){
                mUVCReciver.markingSuccessCaptureFrameMat();
            }
        }
        @Override
        public void onDettach(final UsbDevice device) {
            Logger.i("onDettach");
            LauncherAct.lightOff();
            external_completeUVC();
            statusAttachedUSB = ATTACHMENT_USB_INFO.DETACHED;
            EventBus.getDefault().post(statusAttachedUSB);
        }

        @Override
        public void onCancel(final UsbDevice device) {
            Logger.v("onCancel");
        }

    };

    public static void runTry(Context context){
        final String TAG_CONTEXT = context.getClass().getName();
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(PIService.class.getName().equals(service.service.getClassName())) {
                Log.v(TAG_CONTEXT, "service run");
                return;
            }
        }
        Log.v(TAG_CONTEXT, "start services!");
        context.startService(new Intent(context, PIService.class));
    }
}
