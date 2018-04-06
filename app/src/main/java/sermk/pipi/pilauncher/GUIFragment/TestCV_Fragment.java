package sermk.pipi.pilauncher.GUIFragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.serenegiant.utils.CpuMonitor;
import com.serenegiant.utils.FpsCounter;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import sermk.pipi.pilauncher.CVResolver;
import sermk.pipi.pilauncher.PIService;
import sermk.pipi.pilauncher.R;
import sermk.pipi.pilauncher.LauncherAct;
import sermk.pipi.pilauncher.externalcooperation.AllSettings;


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

        ((ToggleButton)rootView.findViewById(R.id.start_test))
            .setOnCheckedChangeListener(runAndStopUVC_listener);

        mLearnButton = (ToggleButton)rootView.findViewById(R.id.learn_enable);
        mDrawButton = (ToggleButton)rootView.findViewById(R.id.draw_disable);

        mPlotPreview = (CVMaskView)rootView.findViewById(R.id.capture_view);

        posCallbackSeek = (SeekText)rootView.findViewById(R.id.alpha_seek);
        SeekText hDiagSeek = (SeekText)rootView.findViewById(R.id.hdiag_seek);

        TextView text_alpha_seek = (TextView)rootView.findViewById(R.id.text_alpha_seek);
        text_hdiag_seek = (TextView)rootView.findViewById(R.id.text_hdiag_seek);

        posCallbackSeek.setTv(text_alpha_seek);

        hDiagSeek.setTv(text_hdiag_seek);

        ((Button)rootView.findViewById(R.id.clear_button))
            .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlotPreview.clearMask();
            }
        });

        cpuView = (TextView)rootView.findViewById(R.id.cpu_view);
        fpsView = (TextView)rootView.findViewById(R.id.fps_view);

        mFpsCounter = new FpsCounter();
        mFpsCounter.reset();

        ((Button)rootView.findViewById(R.id.start_app)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LauncherAct)getActivity()).runApp();
            }
        });

        ((Button)rootView.findViewById(R.id.save_mask)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllSettings.getInstance().saveMaskInReceiver(getActivity(),
                    mPlotPreview.getByteArrayMask(), mPlotPreview.getRectMaskByte());
            }
        });

        ((Button)rootView.findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllSettings.getInstance().confirmSettings(getActivity());
            }
        });

        ((Button)rootView.findViewById(R.id.clear_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllSettings.getInstance().clear();
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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private final CompoundButton.OnCheckedChangeListener runAndStopUVC_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            Log.v(TAG, "Test CV enable " + isChecked);
            if (isChecked) {
                mPlotPreview.setMask(
                    AllSettings.getInstance().getCurrentSettings().rectMask,
                    AllSettings.getInstance().getBytesMask());
                PIService.startUVCwithCallbackPosition(posCallback);
            } else {
                PIService.external_completeUVC();
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
