package sermk.pipi.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by echormonov on 01.11.17.
 */

public class CVMaskView extends ImageView {

    private final String TAG = "CVMaskView";

    public CVMaskView(Context context) {
        super(context);
    }

    public CVMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        //drawlines where ever you want using canvas.drawLine()
        Log.v(TAG,"onDraw");
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        super.setImageBitmap(bm);

    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {
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
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

}
