package sermk.pipi.pilauncher;

import android.hardware.usb.UsbDevice;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import com.orhanobut.logger.Logger;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;

import sermk.pipi.pilauncher.externalcooperation.AllSettings;

/**
 * Created by ser on 31.10.17.
 */

public class UVCReciver extends Thread implements CVResolver.ICallbackPosition {
    private final String TAG = this.getName();

    private final Object mSyncExit = new Object();

    private enum State { USB_MONITOR_ERROR,
    USB_LIST_EMPTY,
    CAMERA_OPEN_ERROR,
        TRANING_ERROR,
    RELEASE };


    static public class Settings {
        public int width = UVCCamera.DEFAULT_PREVIEW_WIDTH;
        public int height = UVCCamera.DEFAULT_PREVIEW_HEIGHT;
        public int minFps = 25;
        public int maxFps = 30;
        public int frameformat = UVCCamera.FRAME_FORMAT_YUYV;
        public float bandwightFactor = 1.0f;
    }

    Settings uvcSettings;

    private WeakReference<CVResolver> inThreadResolver;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;

    public UVCReciver(final USBMonitor usbMonitor) {
        super("UVCReciver");
        Logger.v(TAG);
        this.mUSBMonitor = usbMonitor;
    }

    public void startCapture(@Nullable final CVResolver.ICallbackPosition callback){
        mCallBackPosition = callback;
        this.start();
        Logger.v("start UVC thread");
    }

    public boolean setCallBackPositionInRun(CVResolver.ICallbackPosition callBackPosition) {
        if(inThreadResolver == null){
            Logger.w("UVC not RAN!");
            return false;
        }
        final CVResolver cvr = inThreadResolver.get();
        if(cvr==null){
            Logger.w("cvresolver not exist!");
            return false;
        }

        mCallBackPosition = callBackPosition;
        cvr.setCallback(mCallBackPosition);
        return true;
    }

    void exitRun(){
        synchronized(mSyncExit) {
            mSyncExit.notifyAll();
        }
        mCallBackPosition = this;
    }

    @Override
    public void run() {
        final int TIMEOUT = 1111;
        final int TRY_MAX = 3;
        int try_start = TRY_MAX;
        while (try_start > 0) {
            final State state = openUVC();
            switch (state){
                case CAMERA_OPEN_ERROR:
                case TRANING_ERROR:
                    try {sleep(TIMEOUT);} catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try_start--;
                    continue;
            }
            break;
        }
        Logger.v("exit run");
    }

    private State openUVC(){
        if (!mUSBMonitor.isRegistered())
            return State.USB_MONITOR_ERROR;
        final List<UsbDevice> list = mUSBMonitor.getDeviceList();
        Logger.v("list size: " + String.valueOf(list.size()));
        if (list.size() == 0) {
            return State.USB_LIST_EMPTY;
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

        State state = State.RELEASE;

        //todo: try - catch - no!
        camera.open(ublock);
        if(!initCamera(camera)){
            state = State.CAMERA_OPEN_ERROR;
        } else {
            final CVResolver cvr = new CVResolver(mCallBackPosition);
            inThreadResolver = new WeakReference<CVResolver>(cvr);

            camera.startPreview();

            if (!tranning(cvr)) {
                state = State.TRANING_ERROR;
            } else {
                waitExit(INF_WAIT);
            } //tranning - success
            cvr.stop();
        } //initCamera - success

        releaseCamera(camera);

        return state;
    }

    private static final int INF_WAIT = 0;

    private boolean waitExit(final int millis){
        synchronized (mSyncExit) {
            try {
                if(millis > 0)
                    mSyncExit.wait(millis);
                else
                    mSyncExit.wait();
            } catch (final InterruptedException e) {
                Logger.e(e, "wait failed");
                return false;
            }
        } //mSyncExit
        return true;
    }

    private void releaseCamera(final UVCCamera camera){
        if (camera != null) {
            camera.stopPreview();
            camera.setStatusCallback(null);
            camera.setButtonCallback(null);
            camera.close();
            camera.destroy();
        }
    }

    boolean initCamera(final UVCCamera camera){
        camera.setStatusCallback(new IStatusCallback() {
            @Override
            public void onStatus(final int statusClass, final int event, final int selector,
                                 final int statusAttribute, final ByteBuffer data) {
                final String status = "onStatus( statusClass= " + statusClass + "; event= " + event
                    + "; selector= " + selector + "; statusAttribute= " + statusAttribute;
                Log.v(TAG, status); }
        });

        final Settings settings = AllSettings.getInstance().getCurrentSettings().uvcSettings;

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
        return true;
    }


    private boolean tranning(final CVResolver cvr){
        countTraningFrame = 0;
        final int WARMING_UP = 1000;
        final int TRANNING_TIME = 1111;
        final int COUNT_MIN = 11;

        cvr.setCallback(this);

        Logger.v( "warming-up start time " + String.valueOf(WARMING_UP));
        if(!waitExit(WARMING_UP))
            return false;
        Log.v(TAG, "warming-up stop");

        Logger.v(  "Start tranning!");
        cvr.setMode(cvr.MODE_LEARN);
        if(!waitExit(TRANNING_TIME))
            return false;

        Logger.v(  "Stop tranning, Start capturing!");
        cvr.setMode(cvr.MODE_CAPTURE);

        if(countTraningFrame < COUNT_MIN){
            Log.w(TAG,"callback don't call!");
            return false;
        }

        cvr.setCallback(mCallBackPosition);
        return true;
    }

    int countTraningFrame = 0;

    @Override
    public boolean callbackPosition(int pos, CVResolver cvr) {
        countTraningFrame++;
        Log.v(TAG,"!");
        return true;
    }

    private CVResolver.ICallbackPosition mCallBackPosition = this;

}
