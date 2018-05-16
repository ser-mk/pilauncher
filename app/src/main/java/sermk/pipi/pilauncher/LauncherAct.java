package sermk.pipi.pilauncher;

import android.app.Activity;
import android.app.Fragment;
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
import sermk.pipi.pilib.AppRunner;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.MClient;
import sermk.pipi.pilib.WatchConnectionMClient;


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
    protected void onStart() {
        //standTo();
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        //standTo();
        rest();
        super.onResume();
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
        Log.v(TAG, "status " + state);
        if(state.equals(State.STAND_TO)){
            Log.v(TAG, "Wake Up!!!" );
            Toast.makeText(this, "player came", Toast.LENGTH_LONG).show();
            standTo();
        } else {
            Log.v(TAG, "REST!!!" );
            Toast.makeText(this, "player left", Toast.LENGTH_LONG).show();
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
        AppRunner.onGameResult(requestCode,resultCode,data);
    }


    public static void tryStartGame(){
        EventBus.getDefault().post(RUN_APP.START);
    }

    private enum RUN_APP {START};

    @Subscribe
    public void tryRunGame(RUN_APP start){
        if (StatusFragment.getWatcherMC(this).checkTimeout()){
            Toast.makeText(this, "WIFI connection problem", Toast.LENGTH_LONG).show();
            return;
        }

        Fragment currFragment = getFragmentManager().findFragmentById(R.id.container);
        if(currFragment instanceof StatusFragment){
            runGame();
            return;
        }

        Log.i(TAG, "unvalible start game!");
    }

    public boolean runGame() {
        final String NAME_GAME_PACKAGE = PiSettings.getInstance().
            getCurrentSettings().behaviorSettings.NAME_GAME_PACKAGE;
        final String NAME_GAME_ACTIVITY = PiSettings.getInstance().
            getCurrentSettings().behaviorSettings.NAME_GAME_ACTIVITY;
        if (!AppRunner.run(this,NAME_GAME_PACKAGE,NAME_GAME_ACTIVITY)){
            Toast.makeText(this, "not found game", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
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

    @Override
    public void onBackPressed() {
        Log.v(TAG,"onBackPressed");
        Fragment currFragment = getFragmentManager().findFragmentById(R.id.container);
        if(currFragment instanceof StatusFragment){
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new PasswordFragment())
                .addToBackStack("")
                .commit();
            return;
        }
        super.onBackPressed();
    }
}
