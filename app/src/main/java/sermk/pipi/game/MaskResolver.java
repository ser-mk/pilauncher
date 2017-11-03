package sermk.pipi.game;

import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import org.opencv.android.Utils;
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

final class MaskResolver {

    static public Bitmap maskVeiw = null;
    static public Rect rectMaskByte = null;
    static public Mat roiMask = null;


    static android.graphics.Rect findMaskCounter(final Bitmap inMask){
        Mat color = new Mat();
        Mat bw = new Mat();
        Utils.bitmapToMat(inMask,color);
        Imgproc.cvtColor(color,bw,Imgproc.COLOR_RGB2GRAY,1);
        color.release();

        Logger.v("channel :" + String.valueOf(bw.channels()));
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(bw,contours,hierarchy,
                Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        Logger.v("qty contourse :" + String.valueOf(contours.size()));
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
        Imgproc.drawContours(bw,contours,maxValIdx,Scalar.all(255));
        roiMask = bw.submat(rect);

        bw.release();

        android.graphics.Rect rectOfMask = new android.graphics.Rect();
        rectOfMask.set(
                rect.x, rect.y,
                rect.x + rect.width, rect.y + rect.height
        );
        return rectOfMask;
    }

}
