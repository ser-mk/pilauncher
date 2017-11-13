package sermk.pipi.game;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.orhanobut.logger.Logger;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.utils.FpsCounter;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

/**
 * Created by echormonov on 31.10.17.
 */

final class CVResolver {

    static public class Settings{
        CVMaskView captureView = null;
        FpsCounter fpsCounter;
        ImageView chartView;
        ToggleButton learnButton;
        int width = 0; //.getCaptureWitgh();
        int height = 0; //.getCaptureHeight();
        int minFps = 0; //.getMinFps();
        int maxFps = 0; //.getMaxFps();
        int frameformat = 0; //.getFrameFormat();
        float bandwightFactor = 0; //.getBandwightFactor();
        Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;
        int pixelFormatCallback = UVCCamera.PIXEL_FORMAT_RGBX;
    }

    private Settings currentSettings;

    private final String TAG = "CVResolver";

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
        /*
        previewRawMat = new Mat(settings.captureView.getHeight(),
            settings.captureView.getWidth(), CvType.CV_8UC2);
        previewRGBMat = new Mat(settings.height,
                settings.width, CvType.CV_8UC3, new Scalar(255,0,0));
        rawBytes = new byte[ settings.height * 2 * settings.width];

        chartMat = new Mat(settings.chartView.getHeight(), settings.chartView.getWidth(),
                CvType.CV_8UC3, new Scalar(0,0,0));
        chartBitmap = Bitmap.createBitmap(settings.chartView.getWidth(),
                settings.chartView.getHeight(), settings.bitmapConfig);
                */
        setPlotOption(previewRGBMat.getNativeObjAddr());
        startCV(true);
    }

    private void plottCV(final long pointMat){
            synchronized (syncPreview) {
                Utils.matToBitmap(previewRGBMat, previewBitmap);
            }

        currentSettings.fpsCounter.count();

        currentSettings.captureView.post(mUpdateImageTask);

        final Rect roiRect = currentSettings.captureView.getRectMaskByte();
        final Mat roiMat = currentSettings.captureView.getRoiMask();

        if(roiMat != null) {
            setRectOfMask(roiRect.x, roiRect.y, roiMat.getNativeObjAddr());
        }
    }

    private void plottCV(final ByteBuffer frame){
        if(frame == null) {
            Logger.e("frame empty");
            return;
        }
        currentSettings.fpsCounter.count();
        frame.get(rawBytes);

        previewRawMat.put(0,0, rawBytes);
        Imgproc.cvtColor(previewRawMat, previewRGBMat,
            Imgproc.COLOR_YUV2RGB_YUYV);
        synchronized (previewBitmap) {
            Utils.matToBitmap(previewRGBMat,previewBitmap);
        }
        currentSettings.captureView.post(mUpdateImageTask);
    }

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

            currentSettings.chartView.post(new Runnable() {
                public void run() {
                    synchronized (chartBitmap) {
                    //Log.v(TAG,"R" + String.valueOf(chartBitmap.getHeight()));
                        currentSettings.chartView.setImageBitmap(chartBitmap);
                    }
                }
            });

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

    public IFrameCallback getIFrameCallback() {
        return mIFrameCallback;
    }

    private static native int passFrameToCVPIPI(final long refMatPreview, final long  refMatChart);
    private static native int passRoiRectToCVPIPI(final int xsRoi, final int ysRoi, final long refMat);
    private static native void enableLearn(final boolean enable);
    //without static for call privat non-static method!
    private native void startCV(final boolean enable);
    private static native void setPlotOption(final long previewMat);
    private static native int setRectOfMask(final int xsRoi, final int ysRoi, final long refMat);

}
