package sermk.pipi.pilauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.orhanobut.logger.Logger;

import sermk.pipi.pilauncher.GUIFragment.TestCV_Fragment;
import sermk.pipi.pilauncher.externalcooperation.ClientWrapper;


public class LauncherAct extends Activity {

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
        startService(new Intent(this, PIService.class));
        ClientWrapper.getInstance().setContext(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.v(
            String.valueOf(requestCode) + " " + String.valueOf(resultCode)
        );
        //bindPIService();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean runApp(){
        //unbindService(mPIConnection);
        final String packageName = "sermk.pipi.ra";
        Logger.v(packageName);
        PackageManager manager = getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                Logger.v("i == null");
                return false;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(0);
            i.putExtra("aaa","111");

            startActivityForResult(i,1);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

}
