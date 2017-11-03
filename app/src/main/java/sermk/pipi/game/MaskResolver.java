package sermk.pipi.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.orhanobut.logger.Logger;

import org.opencv.android.FpsMeter;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ser on 02.11.17.
 */

final class MaskResolver {

    static public Bitmap maskVeiw = null;


    static Bitmap findCounter(final Bitmap inMask){
        Mat color = new Mat();
        Mat bw = new Mat();
        Utils.bitmapToMat(inMask,color);
        Imgproc.cvtColor(color,bw,Imgproc.COLOR_RGB2GRAY,1);
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

        if(maskVeiw == null){
            maskVeiw = Bitmap.createBitmap(inMask);
            maskVeiw.eraseColor(Color.TRANSPARENT);
        }
        maskVeiw.eraseColor(Color.TRANSPARENT);
        Canvas cMV = new Canvas(maskVeiw);
        Paint pMV = new Paint();
        pMV.setColor(Color.BLUE);
        android.graphics.Rect rectOfMask = new android.graphics.Rect();
        rectOfMask.set(
                rect.x, rect.y,
                rect.x + rect.width, rect.y + rect.height
        );

        cMV.drawRect(rectOfMask,pMV);

        return maskVeiw;
    }

}
