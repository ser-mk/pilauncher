package sermk.pipi.pilauncher.GUIFragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import sermk.pipi.pilauncher.BehaviorSettings;
import sermk.pipi.pilauncher.R;
import sermk.pipi.pilauncher.externalcooperation.PiSettings;
import sermk.pipi.pilib.WatchConnectionMClient;


public class StatusFragment extends Fragment implements View.OnClickListener {

    private static final int COUNT_MAX = 11;
    private final String TAG = this.getClass().getName();
    private Timer mTimer;

    private final String CONNECTION_PROBLEM_TITLE
        = "Internet connection problem \r\ncheck Wifi net";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    static int count = 0;

    TextView connectionStatus;
    WatchConnectionMClient watcher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_status, container, false);

        ((ImageView)rootView.findViewById(R.id.image_welcome)).setOnClickListener(this);
        count = 0;

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.v(TAG,"onBackStackChanged");
            }
        });

        connectionStatus = (TextView)rootView.findViewById(R.id.status);
        final BehaviorSettings options = PiSettings.getInstance().getCurrentSettings().behaviorSettings;
        watcher = new WatchConnectionMClient(getActivity(),options.TIMEOUT_MS_FAIL_CONNECTION,
            options.FINE_MS_FAIL_CONNECTION);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer("connection status updating");
        mTimer.schedule(updateStatusTask, 10L, 100L); // интервал - 60000 миллисекунд, 0 миллисекунд до первого запуска.

    }

    private final TimerTask updateStatusTask =  new TimerTask() { // Определяем задачу
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(watcher.checkTimeout()){
                        connectionStatus.setText(CONNECTION_PROBLEM_TITLE);
                    } else {
                        connectionStatus.setText("");
                    }
                }
            });
        }
    };

    @Override
    public void onStop() {
        try {
            mTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG,"onClick");
        count+=1;
        if(count < COUNT_MAX) return;

        Log.v(TAG,"replace");
        getFragmentManager().beginTransaction()
            .replace(R.id.container, new PasswordFragment(), PasswordFragment.class.getName())
            .addToBackStack("")
            .commit();
    }
}
