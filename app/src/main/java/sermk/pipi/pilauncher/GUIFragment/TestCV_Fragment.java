package sermk.pipi.pilauncher.GUIFragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.serenegiant.utils.CpuMonitor;
import com.serenegiant.utils.FpsCounter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import sermk.pipi.pilauncher.CVResolver;
import sermk.pipi.pilauncher.PIService;
import sermk.pipi.pilauncher.R;
import sermk.pipi.pilauncher.LauncherAct;
import sermk.pipi.pilauncher.externalcooperation.PiSettings;
import sermk.pipi.pilauncher.externalcooperation.SettingsReciever;
import sermk.pipi.pilib.CommandCollection;


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

    private ToggleButton mStartStopButton;
    private ToggleButton mLearnButton;
    private ToggleButton mDrawButton;
    private CVMaskView mPlotPreview;
    SeekText posCallbackSeek;
    TextView text_hdiag_seek;
    TextView text_width_pulse_seek;

    TextView cpuView;
    TextView fpsView;

    private final CpuMonitor mCpuMonitor = new CpuMonitor();
    private FpsCounter mFpsCounter;
    private Timer mTimer;

    private EditText minWidthET;
    private EditText maxWidthET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_test_cv_, container, false);

        mStartStopButton = (ToggleButton)rootView.findViewById(R.id.start_test);
        mStartStopButton.setOnCheckedChangeListener(runAndStopUVC_listener);

        mLearnButton = (ToggleButton)rootView.findViewById(R.id.learn_enable);
        mDrawButton = (ToggleButton)rootView.findViewById(R.id.draw_disable);

        mPlotPreview = (CVMaskView)rootView.findViewById(R.id.capture_view);

        posCallbackSeek = (SeekText)rootView.findViewById(R.id.pos_seek);
        SeekText hDiagSeek = (SeekText)rootView.findViewById(R.id.hdiag_seek);

        TextView text_pos_seek = (TextView)rootView.findViewById(R.id.text_pos_seek);
        text_width_pulse_seek = (TextView)rootView.findViewById(R.id.text_width_pulse_seek);
        text_hdiag_seek = (TextView)rootView.findViewById(R.id.text_hdiag_seek);

        posCallbackSeek.setTv(text_pos_seek);

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

        minWidthET = (EditText)rootView.findViewById(R.id.min_widht_pulse_ET);
        maxWidthET = (EditText)rootView.findViewById(R.id.max_widht_pulse_ET);

        getActivity().getWindow().
            setSoftInputMode(WindowManager.
                LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initUISettings();

        mFpsCounter = new FpsCounter();
        mFpsCounter.reset();

        ((Button)rootView.findViewById(R.id.start_app)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LauncherAct)getActivity()).runGame();
            }
        });

        ((Button)rootView.findViewById(R.id.save_mask)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        ((Button)rootView.findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PiSettings.getInstance().confirmSettings(getActivity());
            }
        });

        ((Button)rootView.findViewById(R.id.clear_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PiSettings.getInstance().clear(getActivity());
            }
        });

        ((Button)rootView.findViewById(R.id.not_start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(CommandCollection.ACTION_RECIVER_DPC_NOT_START_COSU_ONE);
                getActivity().sendBroadcast(intent);
            }
        });

        EventBus.getDefault().register(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    private int intervalValue(String val_str, int min, int max){
        int val = min;
        try {
            val = Integer.valueOf(val_str);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(val < min) val = min;
        if(val > max) val = max;

        return val;
    }

    private void initUISettings(){
        final int min = PiSettings.getInstance().getCurrentSettings().behaviorSettings.MIN_PULSE_WIDTH;
        minWidthET.setText(String.valueOf(min));

        final int max = PiSettings.getInstance().getCurrentSettings().behaviorSettings.MAX_PULSE_WIDTH;
        maxWidthET.setText(String.valueOf(max));
    }

    private void saveSettings(){
        final int min = intervalValue(minWidthET.getText().toString(), 0,500);
        final int max = intervalValue(maxWidthET.getText().toString(), min + 1, 1000);

        PiSettings.getInstance().getCurrentSettings().
            behaviorSettings.MAX_PULSE_WIDTH = max;

        PiSettings.getInstance().getCurrentSettings().
            behaviorSettings.MIN_PULSE_WIDTH = min;

        PiSettings.getInstance().saveCurrentSettings(getActivity());

        PiSettings.getInstance().saveMaskInReceiver(getActivity(),
            mPlotPreview.getByteArrayMask(), mPlotPreview.getRectMaskByte());

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
                    PiSettings.getInstance().getCurrentSettings().rectMask,
                    PiSettings.getInstance().getBytesMask());
                PIService.startUVCwithCallbackPosition(posCallback);
            } else {
                PIService.external_completeUVC();
            }
        }
    };

    final CVResolver.ICallbackPosition posCallback = new CVResolver.ICallbackPosition() {
        @Override
        public boolean callbackPosition(final int position, final int width, final CVResolver cvr) {
            if(position > 0){
                final int seek = mPlotPreview.relativePosition(position,posCallbackSeek.getMax());
                posCallbackSeek.setProgress(seek);
                Log.v(TAG,"p=" + String.valueOf(position));
            }

            getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                text_width_pulse_seek.setText(String.valueOf(width));
                                            }
                                        });

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

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showInfoConnection(PIService.CONNECTION_USB_INFO info){
        Log.i(TAG, "info " + info);
        if(info == PIService.CONNECTION_USB_INFO.CONNECTED)
            mStartStopButton.setBackgroundColor(Color.GREEN);
        else
            mStartStopButton.setBackgroundColor(Color.WHITE);
    }
}
