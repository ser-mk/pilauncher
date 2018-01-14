package sermk.pipi.pilauncher.GUIFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import sermk.pipi.pilauncher.CVResolver;


/**
 * Created by ser on 02.11.17.
 */

public class CVMaskResolver extends ImageView {

    protected Rect rectMaskByte;
    protected Mat roiMask;

    public CVMaskResolver(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        clearMask();
    }

    private Mat previewRGBMat;
    protected Bitmap previewBitmap;

    public Rect getRectMaskByte() {
        return rectMaskByte;
    }

    public static byte[] convertMat2BAGRAY(final long ref){
        Mat capMat = new Mat(ref);
        Mat bw = new Mat();
        Imgproc.cvtColor(capMat,bw,Imgproc.COLOR_YUV2GRAY_YUYV);
        byte[] return_buff = new byte[(int) (bw.total() *
            bw.channels())];
        bw.get(0, 0, return_buff);
        return return_buff;
    }

    public static long createRefMat(Rect rect, final byte[] bytes){
        Mat mat = new Mat(rect.height, rect.width, CvType.CV_8UC1);
        mat.put(0,0,bytes);
        return mat.getNativeObjAddr();
    }

    public byte[] getByteArrayMask() {
        byte[] return_buff = new byte[(int) (roiMask.total() *
            roiMask.channels())];
        roiMask.get(0, 0, return_buff);
        return return_buff;
    }

    public void setMask(Rect mask, byte[] bytes){
        rectMaskByte = mask;
        if(roiMask != null) {
            roiMask.release();
        }
        roiMask = new Mat(mask.height, mask.width, CvType.CV_8UC1);
        roiMask.put(0,0,bytes);
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        if(previewRGBMat != null)
            previewRGBMat.release();
        previewRGBMat = new Mat(h, w, CvType.CV_8UC3);
        previewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        CVResolver.setPlotOption(previewRGBMat.getNativeObjAddr());
    }

    protected void clearMask(){
        rectMaskByte = new Rect(0,0,0,0);
        if(roiMask != null) {
            roiMask.release();
        }
        roiMask = new Mat(0,0,0);
    }
    public void cvCallback(final int position, final CVResolver cvr){
        cvr.setRectOfMask(rectMaskByte.x, rectMaskByte.y, roiMask.getNativeObjAddr());
    }
    protected Bitmap getPreviewBitmap(){
        Utils.matToBitmap(previewRGBMat, previewBitmap);
        return previewBitmap;
    }

    protected android.graphics.Rect findMaskCounter(final Bitmap inMask){
        Mat color = new Mat();
        Mat bw = new Mat();
        Utils.bitmapToMat(inMask,color);
        Imgproc.cvtColor(color,bw,Imgproc.COLOR_RGB2GRAY,1);
        color.release();

        //Logger.v("channels :" + String.valueOf(bw.channels()));
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(bw,contours,hierarchy,
                Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        //Logger.v("qty contourse :" + String.valueOf(contours.size()));
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
        {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea)
            {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }

        hierarchy.release();
        MatOfPoint maxContour = contours.get(maxValIdx);
        Rect rect = Imgproc.boundingRect(maxContour);

        rectMaskByte = rect;
        bw.setTo(Scalar.all(0));
        Imgproc.drawContours(bw,contours,maxValIdx,Scalar.all(255), -1);
        Mat submatRect = bw.submat(rect);
        //may be danger
        roiMask = new Mat(submatRect.size(), CvType.CV_8UC1);
        submatRect.copyTo(roiMask);

        bw.release();

        android.graphics.Rect rectOfMask = new android.graphics.Rect();
        rectOfMask.set(
                rect.x, rect.y,
                rect.x + rect.width, rect.y + rect.height
        );
        return rectOfMask;
    }

    protected android.graphics.Rect getViewMask(){
        android.graphics.Rect rectOfMask = new android.graphics.Rect();
        rectOfMask.set(rectMaskByte.x, rectMaskByte.y,
            rectMaskByte.x + rectMaskByte.width,
            rectMaskByte.y + rectMaskByte.height);
        return  rectOfMask;
    }

}
