package sermk.pipi.pilauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import sermk.pipi.pilauncher.GUIFragment.PasswordFragment;
import sermk.pipi.pilauncher.GUIFragment.TestCV_Fragment;
import sermk.pipi.pilauncher.GUIFragment.StatusFragment;
import sermk.pipi.pilauncher.externalcooperation.PiSettings;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.GameRunner;
import sermk.pipi.pilib.MClient;


public class LauncherAct extends Activity implements Thread.UncaughtExceptionHandler {

    private final String TAG = this.getClass().getName();

    private final String TEST_INTENT_FIELD = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) { //todo buildconfig.DEBUG
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standalone);

        /** Problem on T310
         * this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
         */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        final boolean test = this.getIntent().getBooleanExtra(TEST_INTENT_FIELD, false);

        if (test) {
            // do something for a debug build
            getFragmentManager().beginTransaction()
                .add(R.id.container, new TestCV_Fragment()).commit();
        } else {
            getFragmentManager().beginTransaction()
                .add(R.id.container, new StatusFragment()).commit();
        }
        PIService.runTry(this);

        EventBus.getDefault().register(this);

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public enum Screen { TestCV, Password, Status};

    public void addFragment(final Screen screen){
        switch (screen){
            case Status: getFragmentManager().beginTransaction()
                .add(R.id.container, new StatusFragment()).commit();
            case Password:
                getFragmentManager().beginTransaction()
                    .add(R.id.container, new PasswordFragment()).commit();
            case TestCV: getFragmentManager().beginTransaction()
                .add(R.id.container, new TestCV_Fragment()).commit();
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    static public void lightOn(){
        EventBus.getDefault().post(State.STAND_TO);
    }

    static public void lightOff(){
        EventBus.getDefault().post(State.REST);
    }

    private enum State {STAND_TO, REST};

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWakeUpEvent(State state) {
        if (BuildConfig.DEBUG) Toast.makeText(this, "WaKeUp  " + state, Toast.LENGTH_LONG).show();
        Log.v(TAG, "status " + state);
        if(state.equals(State.STAND_TO)){
            Log.v(TAG, "Wake Up!!!" );
            standTo();
        } else {
            Log.v(TAG, "REST!!!" );
            rest();
        }
    }

    private void standTo(){
        PowerManager powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
        PowerManager.WakeLock wake = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);

        wake.acquire();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }

    private void rest(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        GameRunner.onGameResult(requestCode,resultCode,data);
    }

    public boolean runApp() {
        final String NAME_GAME_APP = PiSettings.getInstance().getCurrentSettings().behaviorSettings.NAME_GAME_APP;
        return GameRunner.run(this,NAME_GAME_APP);
    }



    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG,"uncaughtException");

        MClient.sendMessage(this,
            ErrorCollector.subjError(TAG,"uncaughtException"),
            ErrorCollector.getStackTraceString(e));
    /*
        Intent intent = new Intent(this, LauncherAct.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getApplication().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        */
        this.finish();
        System.exit(2);
    }

}
