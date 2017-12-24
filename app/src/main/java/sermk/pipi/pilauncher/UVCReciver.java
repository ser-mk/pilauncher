package sermk.pipi.pilauncher;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.Nullable;
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


    static public class Settings {
        public int width = 0; //.getCaptureWitgh();
        public int height = 0; //.getCaptureHeight();
        public int minFps = 0; //.getMinFps();
        public int maxFps = 0; //.getMaxFps();
        public int frameformat = 0; //.getFrameFormat();
        public float bandwightFactor = 0; //.getBandwightFactor();
    }

    Settings uvcSettings;

    private WeakReference<CVResolver> inThreadResolver;

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

    public void deInit(){
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
            mUSBMonitor = null;
        }
    }

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

    public void startCapture(final Settings settings,@Nullable final CVResolver.ICallbackPosition callback){
        mCallBackPosition = callback;
        uvcSettings = settings;
        this.start();
        Logger.v("start UVC thread");
    }

    public boolean setCallBackPositionInRun(CVResolver.ICallbackPosition callBackPosition) {
        final CVResolver cvr = inThreadResolver.get();
        if(cvr==null){
            Logger.w("cvresolver not exist!");
            return false;
        }

        cvr.setCallback(callBackPosition);
        return true;
        //this.mCallBackPosition = mCallBackPosition;
    }

    void exitRun(){
        synchronized(mSyncExit) {
            mSyncExit.notifyAll();
        }
        mCallBackPosition = defaultCallBackPosition;
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
        //todo: try - catch
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
            Logger.e(e1, "camera IllegalArgumentException!");
            return false;
        } catch (Exception e) {
            Logger.e(e,"camera common exaption!");
            camera.destroy();
            return false;
        }

        final CVResolver cvr = new CVResolver(mCallBackPosition);
        inThreadResolver = new WeakReference<CVResolver>(cvr);

        camera.startPreview();

        synchronized (mSyncExit) {
            try {
                mSyncExit.wait();
            } catch (final InterruptedException e) {
                Logger.e(e,"exit failed");
            }
        }

        cvr.stop();

        if (camera != null) {
            camera.stopPreview();
            camera.setStatusCallback(null);
            camera.setButtonCallback(null);
            camera.close();
            camera.destroy();
        }

        return true;
    }

    CVResolver.ICallbackPosition mCallBackPosition = null;

    final CVResolver.ICallbackPosition defaultCallBackPosition = new CVResolver.ICallbackPosition() {
        @Override
        public boolean callbackPosition(final int pos, final CVResolver cvr ) {
            Logger.v("!");
            return true;
        }
    };

}