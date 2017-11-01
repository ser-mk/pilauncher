package sermk.pipi.game;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by ser on 31.10.17.
 */

public class UVCReciver extends Thread {
    private final String TAG = "UVCReciver";

    private Context gContext;

    private final Object mSync = new Object();
    private final Object mSyncExit = new Object();

    public static final int MSG_OPEN = 0;
    public static final int MSG_CLOSE = 1;
    public static final int MSG_PREVIEW_START = 2;
    public static final int MSG_PREVIEW_STOP = 3;
    public static final int MSG_CAPTURE_STILL = 4;
    public static final int MSG_CAPTURE_START = 5;
    public static final int MSG_CAPTURE_STOP = 6;
    public static final int MSG_MEDIA_UPDATE = 7;
    public static final int MSG_RELEASE = 9;

    CVResolver.Settings cvrSettings;



    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;

    public UVCReciver(final Context context) {
        super("UVCReciver");
        Logger.v(TAG);
        this.mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
        gContext = context;
    }
/* */
    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
    @Override
    public void onAttach(final UsbDevice device) {
        Toast.makeText(gContext, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        Logger.v("onAttach");
    }

    @Override
    public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
        Toast.makeText(gContext, "USB_DEVICE_ONCONNECT", Toast.LENGTH_SHORT).show();
        Logger.v("onConnect");
    }

    @Override
    public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
        Logger.v("onDisconnect");
    }
    @Override
    public void onDettach(final UsbDevice device) {
        Toast.makeText(gContext, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        Logger.v("onDettach");
    }

    @Override
    public void onCancel(final UsbDevice device) {
        Logger.v("onCancel");
    }

    };

    public void startCapture(final CVResolver.Settings settings){
        cvrSettings = settings;
        this.start();
        /*
        synchronized (mSync) {
                try {
                    mSync.wait();
                } catch (final InterruptedException e) {
                    Logger.e(e,"exc",null);
                }
        }
        */
        Logger.v("start UVC thread");
    }

    void exitRun(){
        synchronized(mSyncExit) {
            mSyncExit.notifyAll();
        }
    }

    @Override
    public void run() {
        openUVC(cvrSettings);
        Logger.v("exit run");
    }

    private boolean openUVC(final CVResolver.Settings settings){
        if (!mUSBMonitor.isRegistered())
            return false;
        final List<UsbDevice> list = mUSBMonitor.getDeviceList();
        Logger.v("list size: " + String.valueOf(list.size()));
        if (list.size() == 0) {
            return false;
        }

        UsbDevice device =  list.get(0);

        mUSBMonitor.requestPermission(device);

        CVResolver cvr = new CVResolver(settings);

        final UsbControlBlock ublock = mUSBMonitor.openDevice(device);
        final UVCCamera camera = new UVCCamera();
        camera.open(ublock);
        camera.setStatusCallback(new IStatusCallback() {
            @Override
            public void onStatus(final int statusClass, final int event, final int selector,
                                 final int statusAttribute, final ByteBuffer data) {
                Toast.makeText(gContext, "onStatus(statusClass=" + statusClass
                                + "; " +
                                "event=" + event + "; " +
                                "selector=" + selector + "; " +
                                "statusAttribute=" + statusAttribute + "; " +
                                "data=...)", Toast.LENGTH_SHORT).show();
                    }
                });
        try {
            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
        } catch (final IllegalArgumentException e1) {
            camera.destroy();
            return false;
        }

        camera.setPreviewDisplay((Surface)null);
        camera.setPreviewSize(640,480, 25,30, 0, 1.0f);
        camera.setFrameCallback(cvr.getIFrameCallback(), UVCCamera.PIXEL_FORMAT_RGB565/*UVCCamera.PIXEL_FORMAT_NV21*/);
        camera.startPreview();
        Log.v("!!","1");

        synchronized (mSyncExit) {
            try {
                mSyncExit.wait();
            } catch (final InterruptedException e) {
                Logger.e(e,"exc",null);
            }
        }

        if (camera != null) {
            Log.v("!!","2");
            camera.stopPreview();
            camera.setStatusCallback(null);
            camera.setButtonCallback(null);
            camera.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_RAW);
            camera.close();
            camera.destroy();
        }

        return true;
    }

    private final class HandlerControl extends Handler {


        private final WeakReference<UVCReciver> mWeakThread;

        public HandlerControl(UVCReciver reciver) {
            this.mWeakThread = new WeakReference<UVCReciver>(reciver);
        }

        @Override
        public void handleMessage(final Message msg) {
            final UVCReciver UVCthread = mWeakThread.get();
            switch (msg.what) {
                case MSG_RELEASE:
                    UVCthread.exitRun();
                    break;
                default:
                    Logger.e("unsupported message:what=" + msg.what);
            }
        }
    };



}
