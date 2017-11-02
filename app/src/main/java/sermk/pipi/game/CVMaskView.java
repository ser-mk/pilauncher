package sermk.pipi.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

/**
 * Created by echormonov on 01.11.17.
 */

public class CVMaskView extends ImageView {

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

    private int alfamask = 111;

    private boolean checkInit(){
        if (mask == null)
            return false;
        return true;
    }

    private TextView alphaTV = null;
    private TextView hDiagTV = null;

    public void setAlphaTV(TextView alphaTV) {
        this.alphaTV = alphaTV;
    }

    public void sethDiagTV(TextView hDiagTV) {
        this.hDiagTV = hDiagTV;
    }

    public void clearMask(){
        if(mask == null) return;
        mask.eraseColor(Color.TRANSPARENT);
    }

    private void drawRectFrCenter(float x, float y){
        if(alphaTV != null ) {
            String str = alphaTV.getText().toString();
            try{
                int num = Integer.parseInt(str);
                renderPaint.setAlpha(num);
            } catch (NumberFormatException e) {
                Logger.w("it's not int param " + str);
            }
        }
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

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        if(mask == null){
            mask = Bitmap.createBitmap(bm);
            //mask.setConfig(Bitmap.Config.RGB_565);
            clearMask();
            canvasMask = new Canvas(mask);
            maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            maskPaint.setColor(Color.RED);

            result = Bitmap.createBitmap(bm);
            result.eraseColor(Color.TRANSPARENT);
            canvasResult = new Canvas(result);
            renderPaint = new Paint();
            renderPaint.setAlpha(111);

            drawRectFrCenter(111,111);

            Logger.v("create mask type:" + String.valueOf(result.getConfig()));
        }

        canvasResult.drawBitmap(bm,0,0,null);
        canvasResult.drawBitmap(mask,0,0,renderPaint);

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
                Log.v(TAG,"ACTION_DOWN " + String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(w) + " " + String.valueOf(h));
            case MotionEvent.ACTION_MOVE:
                drawRectFrCenter(x,y);
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG,"ACTION_UP " + String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(w) + " " + String.valueOf(h));
                break;
        }

        return true;
    }

    public void setHalfDiagonal(final float halfDiagonal) {
        this.halfDiagonal = halfDiagonal;
    }

    public void setAlfamask(final int alfamask) {
        this.alfamask = alfamask;
    }
}
