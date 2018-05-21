package sermk.pipi.pilauncher.GUIFragment;

import android.app.FragmentManager;
import android.content.Context;
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
import sermk.pipi.pilauncher.LauncherAct;
import sermk.pipi.pilauncher.PIService;
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
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_status, container, false);

        imageView = (ImageView)rootView.findViewById(R.id.image_welcome); //.setOnClickListener(this);
        count = 0;

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.v(TAG,"onBackStackChanged");
            }
        });

        connectionStatus = (TextView)rootView.findViewById(R.id.status);

        watcher = getWatcherMC(getActivity());

        return rootView;
    }

    public static WatchConnectionMClient getWatcherMC(Context context){
        final BehaviorSettings options = PiSettings.getInstance().getCurrentSettings().behaviorSettings;
        return new WatchConnectionMClient(context,options.TIMEOUT_MS_FAIL_CONNECTION,
            options.FINE_MS_FAIL_CONNECTION);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer("connection status updating");
        mTimer.schedule(new UpdateStatusTask(), 10L, 100L);
    }

    private class UpdateStatusTask extends TimerTask {
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
                    if(PIService.getStatusUSB() == PIService.CONNECTION_USB_INFO.CONNECTED)
                        imageView.setImageResource(R.drawable.start_game);
                    else
                        imageView.setImageResource(R.drawable.lets);
                }
            });
        }
    }

    @Override
    public void onStop() {
        try {
            mTimer.cancel();
            mTimer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG,"onClick");
        /*
        count+=1;
        if(count < COUNT_MAX) return;

        Log.v(TAG,"replace");
        getFragmentManager().beginTransaction()
            .replace(R.id.container, new PasswordFragment(), PasswordFragment.class.getName())
            .addToBackStack("")
            .commit();
            */
        LauncherAct.tryStartGame();
    }
}
