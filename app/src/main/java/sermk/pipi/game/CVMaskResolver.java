package sermk.pipi.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ser on 02.11.17.
 */

class CVMaskResolver extends ImageView {

    protected Rect rectMaskByte = null;
    protected Mat roiMask = null;

    public CVMaskResolver(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Rect getRectMaskByte() {
        return rectMaskByte;
    }

    public Mat getRoiMask() {
        return roiMask;
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

}
