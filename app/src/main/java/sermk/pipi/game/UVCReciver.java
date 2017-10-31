package sermk.pipi.game;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.widget.Toast;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;

import com.orhanobut.logger.Logger;

/**
 * Created by ser on 31.10.17.
 */

public class UVCReciver {

    private Context gContext;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;

    public UVCReciver(final Context context) {
        Logger.v("UVCReciver");
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
        Logger.v("attach");
    }

    @Override
    public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
        Toast.makeText(gContext, "USB_DEVICE_ONCONNECT", Toast.LENGTH_SHORT).show();
        Logger.v("connect");
    }

    @Override
    public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {

    }
    @Override
    public void onDettach(final UsbDevice device) {
        Toast.makeText(gContext, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(final UsbDevice device) {
    }

    };

}
