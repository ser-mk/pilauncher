package sermk.pipi.game;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.orhanobut.logger.Logger;
import com.serenegiant.utils.CpuMonitor;
import com.serenegiant.utils.FpsCounter;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class TestCV_Fragment extends Fragment {

    private final String TAG  = "TestCV_Fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ToggleButton mPreviewButton;
    private ToggleButton mLearnButton;
    private ToggleButton mDrawButton;
    private CVMaskView mPlotPreview;
    SeekText posCallbackSeek;
    TextView text_hdiag_seek;

    TextView cpuView;
    TextView fpsView;

    private final CpuMonitor mCpuMonitor = new CpuMonitor();
    private FpsCounter mFpsCounter;
    private Timer mTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_test_cv_, container, false);

        mPreviewButton = (ToggleButton)rootView.findViewById(R.id.start_test);
        mLearnButton = (ToggleButton)rootView.findViewById(R.id.learn_enable);
        mDrawButton = (ToggleButton)rootView.findViewById(R.id.draw_disable);
        setToogleButton(false);

        mPreviewButton.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mPlotPreview = (CVMaskView)rootView.findViewById(R.id.capture_view);
/**/
        posCallbackSeek = (SeekText)rootView.findViewById(R.id.alpha_seek);
        SeekText hDiagSeek = (SeekText)rootView.findViewById(R.id.hdiag_seek);

        TextView text_alpha_seek = (TextView)rootView.findViewById(R.id.text_alpha_seek);
        text_hdiag_seek = (TextView)rootView.findViewById(R.id.text_hdiag_seek);

        posCallbackSeek.setTv(text_alpha_seek);

        hDiagSeek.setTv(text_hdiag_seek);

        //mPlotPreview.sethDiagTV(text_hdiag_seek);
        //mPlotPreview.setPosSeek(posCallbackSeek);

        Button clear = (Button)rootView.findViewById(R.id.clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlotPreview.clearMask();
            }
        });

        cpuView = (TextView)rootView.findViewById(R.id.cpu_view);
        fpsView = (TextView)rootView.findViewById(R.id.fps_view);

        mFpsCounter = new FpsCounter();
        mFpsCounter.reset();

        Button runButton = (Button)rootView.findViewById(R.id.start_app);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Standalone)getActivity()).runApp();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        mTimer = new Timer("cpu_fps_timer");
        mTimer.schedule(new TimerTask() { // Определяем задачу
                            @Override
                            public void run() {
                                cpuView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        cpuView.setText(String.format(Locale.US, "GPU:%3d/%3d/%3d",
                                            mCpuMonitor.getCpuCurrent(),
                                            mCpuMonitor.getCpuAvg3(),
                                            mCpuMonitor.getCpuAvgAll()));
                                    }
                                });
                                fpsView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFpsCounter.update();
                                        fpsView.setText(String.format(Locale.US, "CDR:%4.1f", mFpsCounter.getFps()));
                                    }
                                });
                            }
                        }, 1111L,
            getResources().getInteger(R.integer.cpu_timer_period)); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onStop() {
        try {
            mTimer.cancel();
            //mTimer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    private void setToogleButton(final boolean onoff) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewButton.setOnCheckedChangeListener(null);
                mLearnButton.setOnCheckedChangeListener(null);
                try {
                    mPreviewButton.setChecked(onoff);
                    mLearnButton.setChecked(onoff);
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            Log.v(TAG, "Test CV enable " + isChecked);
            GlobalController app = (GlobalController)getActivity().getApplication();
            if (isChecked) {
                UVCReciver.Settings settings = new UVCReciver.Settings();
                app.getGlobalSettings().setUVCSettings(settings);
                PIService.getInstance().startUVC(settings,posCallback);
            } else {
                PIService.getInstance().completeUVC();
            }
        }
    };

    final CVResolver.ICallbackPosition posCallback = new CVResolver.ICallbackPosition() {
        @Override
        public boolean callbackPosition(final int position, final CVResolver cvr) {
            if(position > 0){
                final int seek = mPlotPreview.relativePosition(position,posCallbackSeek.getMax());
                posCallbackSeek.setProgress(seek);
                Log.v(TAG,"p=" + String.valueOf(position));
            }

            mFpsCounter.count();

            final boolean learnEnable = mLearnButton.isChecked();
            if(learnEnable){
                cvr.setMode(cvr.MODE_LEARN);
            } else {
                cvr.setMode(cvr.MODE_CAPTURE);
            }

            final boolean drawDisable = mDrawButton.isChecked();
            cvr.setDisablePlot(drawDisable);

            if(drawDisable){
                return true;
            }
            final String str = text_hdiag_seek.getText().toString();
            mPlotPreview.setHdiag(str);

            mPlotPreview.cvCallback(position,cvr);
            return true;

        }
    };
}
