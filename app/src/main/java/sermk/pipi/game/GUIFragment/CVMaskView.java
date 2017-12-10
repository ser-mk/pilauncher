package sermk.pipi.game.GUIFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.orhanobut.logger.Logger;

import sermk.pipi.game.CVResolver;
import sermk.pipi.game.GUIFragment.CVMaskResolver;

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
    private float halfDiagonal = 33;
    private Rect rectOfMask = null;
    private Paint pRectMask;

    private boolean checkInit(){
        if (mask == null)
            return false;
        return true;
    }

    private int position = -1;
    private Paint positionPaint = new Paint();

    public void clearMask(){
        super.clearMask();
        if(mask == null) return;
        mask.eraseColor(Color.TRANSPARENT);
        rectOfMask = null;
        position = -1;
    }


    public void setHdiag(final String str){
        try{
            int num = Integer.parseInt(str);
            halfDiagonal = (float)num;
        } catch (NumberFormatException e) {
            Logger.w("it's not float param " + str);
            halfDiagonal = 0;
        }
    }

    private void drawRectFrCenter(float x, float y){
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

    public int relativePosition(final int position, final int maxValue){
        if(position < 0)
            return 0;
        if(rectOfMask == null)
            return 0;

        final int width = rectOfMask.width();
        if(position > width)
            return maxValue;
        final int relativePos = (position * maxValue )/ width;
        return relativePos;
    }

    @Override
    protected void onDraw(Canvas canvasResult) {
        canvasResult.drawBitmap(super.getPreviewBitmap(),0,0,null);

        canvasResult.drawBitmap(mask,0,0,renderPaint);

        if(rectOfMask != null){
            canvasResult.drawRect(rectOfMask,pRectMask);
        }

        if(position > 0){
            drawPosition();
        }

        super.onDraw(canvasResult);
    }

    @Override
    public void cvCallback(final int position, final CVResolver cvr){
        super.cvCallback(position,cvr);
        this.position = position;
        this.postInvalidate();
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        clearMask();
        canvasMask = new Canvas(mask);
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.RED);

        result = Bitmap.createBitmap(mask);
        result.eraseColor(Color.TRANSPARENT);
        canvasResult = new Canvas(result);
        renderPaint = new Paint();
        renderPaint.setAlpha(70);

        pRectMask = new Paint();
        pRectMask.setColor(Color.GREEN);
        pRectMask.setStyle(Paint.Style.STROKE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if(!checkInit())
            return true;

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        final float x = e.getX();
        final float y = e.getY();

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