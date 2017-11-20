package sermk.pipi.game;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.orhanobut.logger.Logger;


public class Standalone extends Activity {

    private PIService mPIService;
    boolean mBoundPI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standalone);


        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new TestCV_Fragment()).commit();

        Logger.v("start services");
        //startService(new Intent(this, NotifyService.class));
        startService(new Intent(this, PIService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PIService.class);
        final boolean result = bindService(intent, mPIConnection, Context.BIND_AUTO_CREATE);
        Logger.v("bind service " + String.valueOf(result));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.v(
            String.valueOf(requestCode) + " " + String.valueOf(resultCode)
        );
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mPIConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Logger.v("PI onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PIService.LocalBinder binder = (PIService.LocalBinder) service;
            mPIService = binder.getService();
            mBoundPI = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundPI = false;
            Logger.v("PI onServiceDisconnected");
        }
    };

    public PIService getPIService() {
        return mPIService;
    }
}
