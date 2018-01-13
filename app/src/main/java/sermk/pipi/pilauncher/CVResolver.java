package sermk.pipi.pilauncher;

import android.support.annotation.Nullable;

/**
 * Created by echormonov on 31.10.17.
 */

public final class CVResolver {

    public interface ICallbackPosition {
        public boolean callbackPosition(final int pos, final CVResolver cvr);
    }

    private final String TAG = "CVResolver";

    public final int MODE_CAPTURE = 0;
    public final int MODE_LEARN = 1;

    public final int CAPTURE_ONE_FRAME_START_TO = 0;
    public final int CAPTURE_ONE_FRAME_RETURN_RESULT = 1;

    private ICallbackPosition mICallbackPosition = null;

    public CVResolver(@Nullable final ICallbackPosition callback) {
        setCallback(callback);
        setMode(MODE_CAPTURE);
        startCV(false);
    }

    public void setCallback(@Nullable final ICallbackPosition callback) {
        this.mICallbackPosition = callback;
    }

    private void plottCV(final int position){
        if(this.mICallbackPosition == null){
            return;
        }
        this.mICallbackPosition.callbackPosition(position, this);
    }

    public native void startCV(final boolean enable);

    public static native int setRectOfMask(final int xsRoi, final int ysRoi, final long refMat);
    public static native void setMode(final int mode);
    public static native long getFrame(final int mode);
    public void stop(){}

    public static native void setDisablePlot(boolean disable);
    public static native void setPlotOption(final long previewMat);
}
