package sermk.pipi.game;

import android.graphics.Bitmap;
import android.util.Log;

import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;

/**
 * Created by echormonov on 31.10.17.
 */

final class CVResolver {

    static public class Settings{
        CVMaskView captureView = null;
        int width = 0; //.getCaptureWitgh();
        int height = 0; //.getCaptureHeight();
        int minFps = 0; //.getMinFps();
        int maxFps = 0; //.getMaxFps();
        int frameformat = 0; //.getFrameFormat();
        float bandwightFactor = 0; //.getBandwightFactor();
        Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;
    }

    private Settings currentSettings;

    private final String TAG = "CVResolver";

    Bitmap previewBitmap;// = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, Bitmap.Config.RGB_565);

    public CVResolver(final Settings settings) {
        currentSettings = new Settings();
        if (settings == null)
            return;
        if(settings.captureView != null ){
            currentSettings.captureView = settings.captureView;
        }
        previewBitmap = Bitmap.createBitmap(settings.width,
                settings.height, settings.bitmapConfig);
    }


    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            Log.v(TAG,"capture frame");
            if(currentSettings.captureView == null)
                return;
            frame.clear();
            synchronized (previewBitmap) {
                previewBitmap.copyPixelsFromBuffer(frame);
            }
            //mFpsCounter.count();

            currentSettings.captureView.post(mUpdateImageTask);
        }
    };

    private final Runnable mUpdateImageTask = new Runnable() {
        @Override
        public void run() {
            synchronized (previewBitmap) {
                currentSettings.captureView.setImageBitmap(previewBitmap);
            }
        }
    };

    public IFrameCallback getIFrameCallback() {
        return mIFrameCallback;
    }

}
