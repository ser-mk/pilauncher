package sermk.pipi.pilauncher.GUIFragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by echormonov on 02.11.17.
 */

final public class SeekText extends SeekBar {

    private TextView tv;

    public void setTv(TextView tv) {
        this.tv = tv;
    }

    public SeekText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(tv == null) return;
                tv.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(tv == null) return;
                tv.setText(String.valueOf(seekBar.getProgress()));
            }
        });
    }
}
