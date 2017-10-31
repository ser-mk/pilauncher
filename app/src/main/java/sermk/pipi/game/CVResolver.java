package sermk.pipi.game;

import android.util.Log;
import android.widget.ImageView;

import com.serenegiant.usb.IFrameCallback;

import java.nio.ByteBuffer;

/**
 * Created by echormonov on 31.10.17.
 */

final class CVResolver {

    public class Settings{
        public ImageView captureView;
    }

    private final String TAG = CVResolver;

    public CVResolver(Settings settings) {

    }

    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            Log.v(TAG,"captue frame");
        }
    };
}
