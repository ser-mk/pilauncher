package sermk.pipi.game;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.Surface;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

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

    static public class Settings {
        int width = 0; //.getCaptureWitgh();
        int height = 0; //.getCaptureHeight();
        int minFps = 0; //.getMinFps();
        int maxFps = 0; //.getMaxFps();
        int frameformat = 0; //.getFrameFormat();
        float bandwightFactor = 0; //.getBandwightFactor();
    }

    Settings uvcSettings;

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
        synchronized(mSyncExit) {
            mSyncExit.notifyAll();
        }
    }

    @Override
    public void onCancel(final UsbDevice device) {
        Logger.v("onCancel");
    }

    };

    public void startCapture(final Settings settings){
        uvcSettings = settings;
        this.start();
        Logger.v("start UVC thread");
    }

    void exitRun(){
        synchronized(mSyncExit) {
            mSyncExit.notifyAll();
        }
    }

    @Override
    public void run() {
        openUVC(uvcSettings);
        Logger.v("exit run");
    }

    private boolean openUVC(final Settings settings){
        if (!mUSBMonitor.isRegistered())
            return false;
        final List<UsbDevice> list = mUSBMonitor.getDeviceList();
        Logger.v("list size: " + String.valueOf(list.size()));
        if (list.size() == 0) {
            return false;
        }
        UsbDevice device =  list.get(0);

        Logger.v("getDeviceClass :" +
                String.valueOf(device.getDeviceClass()) +" did: "
                + String.valueOf(device.getDeviceId()) + " PId: "
                + String.valueOf(device.getProductId()) + " Vid"
                + String.valueOf(device.getVendorId() ) + " name: "
                + device.getDeviceName()
        );

        mUSBMonitor.requestPermission(device);

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
                Logger.v("onStatus(statusClass=" + statusClass
                        + "; " +
                        "event=" + event + "; " +
                        "selector=" + selector + "; " +
                        "statusAttribute=" + statusAttribute + "; " +
                        "data=...)");
                    }
                });

        if (settings == null) return false;

        try {
            camera.setPreviewDisplay((Surface)null);
            camera.setPreviewSize(settings.width,settings.height, settings.minFps,
                    settings.maxFps, settings.frameformat, settings.bandwightFactor);
        } catch (final IllegalArgumentException e1) {
            camera.destroy();
            return false;
        } catch (Exception e) {
            camera.destroy();
            return false;
        }

        camera.startPreview();

        synchronized (mSyncExit) {
            try {
                mSyncExit.wait();
            } catch (final InterruptedException e) {
                Logger.e(e,"exc");
            }
        }

        if (camera != null) {
            camera.stopPreview();
            camera.setStatusCallback(null);
            camera.setButtonCallback(null);
            camera.setFrameCallback(null, UVCCamera.PIXEL_FORMAT_RAW);
            camera.close();
            camera.destroy();
        }

        return true;
    }

}
