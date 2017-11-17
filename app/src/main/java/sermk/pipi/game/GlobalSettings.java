package sermk.pipi.game;

import android.content.Context;
import android.graphics.Bitmap;

import com.serenegiant.usb.UVCCamera;

/**
 * Created by ser on 02.11.17.
 */

public final class GlobalSettings {

    private Context gContext;

    public GlobalSettings(final Context context) { this.gContext = context; }

    public int getCaptureWitgh() { return UVCCamera.DEFAULT_PREVIEW_WIDTH; }
    public int getCaptureHeight() { return UVCCamera.DEFAULT_PREVIEW_HEIGHT; }
    public int getMinFps() { return 25; }
    public int getMaxFps() { return 30; }
    public int getFrameFormat() { return UVCCamera.FRAME_FORMAT_YUYV; }
    public float getBandwightFactor() { return 1.0f; }
    
    void setUVCSettings(UVCReciver.Settings settings){
        settings.width = getCaptureWitgh();
        settings.height = getCaptureHeight();
        settings.minFps = getMinFps();
        settings.maxFps = getMaxFps();
        settings.frameformat = getFrameFormat();
        settings.bandwightFactor = getBandwightFactor();
    }

}
