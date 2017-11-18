package sermk.pipi.game;

import android.support.annotation.Nullable;

/**
 * Created by echormonov on 31.10.17.
 */

final class CVResolver {

    public interface ICallbackPosition {
        public boolean callbackPosition(final int pos, final CVResolver cvr);
    }

    private final String TAG = "CVResolver";

    public final int MODE_CAPTURE = 0;
    public final int MODE_LEARN = 1;

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

    /*
        static public class Settings{
            CVMaskView captureView = null;
            FpsCounter fpsCounter;
            ToggleButton learnButton;
            ToggleButton drawButton;
            int width = 0; //.getCaptureWitgh();
            int height = 0; //.getCaptureHeight();
            int minFps = 0; //.getMinFps();
            int maxFps = 0; //.getMaxFps();
            int frameformat = 0; //.getFrameFormat();
            float bandwightFactor = 0; //.getBandwightFactor();
            Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;
            int pixelFormatCallback = UVCCamera.PIXEL_FORMAT_RGBX;
        }

        private Settings currentSettings = null;

        Bitmap previewBitmap;// = Bitmap.createBitmap(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
        final private Object syncPreview = new Object();;
        byte[] rawBytes;
        Mat previewRawMat;
        Mat previewRGBMat;
        Mat chartMat;
        Bitmap chartBitmap;
        public CVResolver(final Settings settings) {
            currentSettings = new Settings();
            if (settings == null)
                return;
            currentSettings = settings;
            previewBitmap = Bitmap.createBitmap(settings.captureView.getWidth(),
                    settings.captureView.getHeight(), settings.bitmapConfig);
            previewRGBMat = new Mat(settings.captureView.getHeight(),
                settings.captureView.getWidth(), CvType.CV_8UC3, new Scalar(255,0,0));

            setPlotOption(previewRGBMat.getNativeObjAddr());
            setMode(MODE_CAPTURE);
            startCV(true);
        }

        private void setPosition(final int position){

        }

        private void plottCV(final int position){

            if(position > 0){
                currentSettings.captureView.setPosition(position);
                //currentSettings.captureView.seekPosition(position);
                Logger.v("position: " + String.valueOf(position));
            }

            final boolean drawDisable = currentSettings.drawButton.isChecked();
            setDisablePlot(drawDisable);

            if(drawDisable){
                Logger.v("draw Disable");
                return;
            }

                synchronized (syncPreview) {
                    Utils.matToBitmap(previewRGBMat, previewBitmap);
                }

            currentSettings.fpsCounter.count();

            //currentSettings.captureView.post(mUpdateImageTask);

            final Rect roiRect = currentSettings.captureView.getRectMask();
            final Mat roiMat = currentSettings.captureView.getMatMask();

            if(roiMat != null) {
                setRectOfMask(roiRect.x, roiRect.y, roiMat.getNativeObjAddr());
            }
            final boolean learnEnable = currentSettings.learnButton.isChecked();
            if(learnEnable){
                setMode(MODE_LEARN);
            } else {
                setMode(MODE_CAPTURE);
            }

        }

    /*
        private final IFrameCallback mIFrameCallback = new IFrameCallback() {
            @Override
            public void onFrame(final ByteBuffer frame) {

                frame.get(rawBytes);
                previewRawMat.put(0,0, rawBytes);
                Imgproc.cvtColor(previewRawMat, previewRGBMat,
                        Imgproc.COLOR_YUV2RGB_YUYV);

                currentSettings.fpsCounter.count();

                //ToDo: bitmap must final !
                synchronized (previewBitmap) {
                    Utils.matToBitmap(previewRGBMat,previewBitmap);
                }

                currentSettings.captureView.post(mUpdateImageTask);

                final Rect roiRect = currentSettings.captureView.getRectMaskByte();
                final Mat roiMat = currentSettings.captureView.getRoiMask();

                if(roiMat == null)
                    return;

                final boolean learnEnable = currentSettings.learnButton.isChecked();

                enableLearn(learnEnable);
                final int res = passRoiRectToCVPIPI(roiRect.x, roiRect.y, roiMat.getNativeObjAddr());
                final int pos = passFrameToCVPIPI(previewRawMat.getNativeObjAddr(), chartMat.getNativeObjAddr());

                currentSettings.captureView.setPosition(pos);

                //ToDo: bitmap must final !
                synchronized (chartBitmap) {
                    //chartBitmap = Bitmap.createBitmap(chartMat.width(), chartMat.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(chartMat, chartBitmap);
                }

            }
        };

        private final Runnable mUpdateImageTask = new Runnable() {
            @Override
            public void run() {
                synchronized (syncPreview) {
                    currentSettings.captureView
                            .setImageBitmap(previewBitmap);
                }
            }
        };
    /*
        private static native int passFrameToCVPIPI(final long refMatPreview, final long  refMatChart);
        private static native int passRoiRectToCVPIPI(final int xsRoi, final int ysRoi, final long refMat);
        private static native void enableLearn(final boolean enable);
    */
    //without static for call privat non-static method!
    public native void startCV(final boolean enable);

    public static native int setRectOfMask(final int xsRoi, final int ysRoi, final long refMat);
    public static native void setMode(final int mode);
    public void stop(){}

    public static native void setDisablePlot(final boolean disable);
    public static native void setPlotOption(final long previewMat);
}
