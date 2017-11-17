package sermk.pipi.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

/**
 * Created by echormonov on 01.11.17.
 */

public class CVMaskView extends CVMaskResolver {

    private final String TAG = "CVMaskView";

    public CVMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Bitmap mask = null;
    private Bitmap result = null;
    private Canvas canvasResult = null;
    private Canvas canvasMask = null;
    private Paint maskPaint = null;
    private Paint renderPaint = null;
    private float halfDiagonal = 111;
    private Rect rectOfMask = null;
    private Paint pRectMask;

    private boolean checkInit(){
        if (mask == null)
            return false;
        return true;
    }

    private TextView hDiagTV = null;

    public void sethDiagTV(TextView hDiagTV) {
        this.hDiagTV = hDiagTV;
    }

    private int position = -1;
    private Paint positionPaint = new Paint();

    public void clearMask(){
        if(mask == null) return;
        mask.eraseColor(Color.TRANSPARENT);
        rectOfMask = null;
        super.rectMaskByte = null;
        if(super.roiMask != null) {
            super.roiMask.release();
            super.roiMask = null;
        }
        position = -1;
    }

    private void drawRectFrCenter(float x, float y){
        if(hDiagTV != null ) {
            String str = hDiagTV.getText().toString();
            try{
                int num = Integer.parseInt(str);
                halfDiagonal = (float)num;
            } catch (NumberFormatException e) {
                Logger.w("it's not float param " + str);
            }
        }
        float top = y - halfDiagonal;
        float bottom = y + halfDiagonal;
        float left = x - halfDiagonal;
        float right = x + halfDiagonal;

        canvasMask.drawRect(left,top,right,bottom,maskPaint);
    }

    private void drawPosition(){
        if(rectOfMask == null)
            return;
        final float top = 0;
        final float bottom = result.getHeight();
        final int w = 20;
        final float left = rectOfMask.left + position - w;
        final float right = left + 2*w;
        positionPaint.setColor(Color.MAGENTA);
        positionPaint.setAlpha(44);
        canvasResult.drawRect(left,top,right,bottom,positionPaint);
    }
/*
    private SeekText posSeek = null;
    public void setPosSeek(SeekText posSeek) {      this.posSeek = posSeek;    }
    public void seekPosition(final int posInMask){
        if(posInMask<0)
            return;
        if(rectOfMask == null)
            return;

        final int width = rectOfMask.width();
        if(posInMask > width)
            return;

        final int maxSeekValue = posSeek.getMax();
        final int pos = (posInMask * maxSeekValue )/ width;
        posSeek.setProgress(pos);
    }
*/
    public void setPosition(final int position) {
        this.position = position;
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if(mask == null){
            mask = Bitmap.createBitmap(bitmap);
            //mask.setConfig(Bitmap.Config.RGB_565);
            clearMask();
            canvasMask = new Canvas(mask);
            maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            maskPaint.setColor(Color.RED);

            result = Bitmap.createBitmap(bitmap);
            result.eraseColor(Color.TRANSPARENT);
            canvasResult = new Canvas(result);
            renderPaint = new Paint();
            renderPaint.setAlpha(70);

            pRectMask = new Paint();
            pRectMask.setColor(Color.GREEN);
            pRectMask.setStyle(Paint.Style.STROKE);
        }

        canvasResult.drawBitmap(bitmap,0,0,null);
        canvasResult.drawBitmap(mask,0,0,renderPaint);

        if(rectOfMask != null){
            canvasResult.drawRect(rectOfMask,pRectMask);
        }

        if(position > 0){
            drawPosition();
        }

        super.setImageBitmap(result);
    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if(!checkInit())
            return true;

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        int w = getWidth();
        int h = getHeight();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.v(TAG,"ACTION_DOWN " + String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(w) + " " + String.valueOf(h));
            case MotionEvent.ACTION_MOVE:
                drawRectFrCenter(x,y);
                break;
            case MotionEvent.ACTION_UP:
                //Log.v(TAG,"ACTION_UP " + String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(w) + " " + String.valueOf(h));
                rectOfMask = super.findMaskCounter(mask);
                break;
        }

        return true;
    }

}

