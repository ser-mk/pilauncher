package sermk.pipi.pilauncher;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import sermk.pipi.pilauncher.GUIFragment.PasswordFragment;
import sermk.pipi.pilauncher.GUIFragment.TestCV_Fragment;
import sermk.pipi.pilauncher.GUIFragment.WelcomeFragment;
import sermk.pipi.pilib.GameRunner;


public class LauncherAct extends Activity implements Thread.UncaughtExceptionHandler {

    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standalone);

        /** Problem on T310
         * this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
         */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new TestCV_Fragment()).commit();
        //.add(R.id.container, new WelcomeFragment()).commit();
            //.add(R.id.container, new PasswordFragment()).commit();

        Logger.v("start services");
        startService(new Intent(this, PIService.class));

        EventBus.getDefault().register(this);
        standTo();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {super.onStart();}

    public enum State {STAND_TO, REST};

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWakeUpEvent(State state) {
        Toast.makeText(this, "WaKeUp  " + state, Toast.LENGTH_LONG).show();
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
        return GameRunner.run(this,"sermk.pipi.testbind");
    }



    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG,"uncaughtException");
        Intent intent = new Intent(this, LauncherAct.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getApplication().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        this.finish();
        System.exit(2);
    }

}
