package sermk.pipi.pilib;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by echormonov on 26.12.17.
 */

public class GameRunner {

    private final static String TAG = "GameRunner";

    private static final int IF_ACTIVITY_EXIST = 1;


    public static void onGameResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG,
            String.valueOf(requestCode) + " " + String.valueOf(resultCode)
        );
    }

    public static boolean run(Activity act, final String packageName){

        Log.v(TAG, packageName);
        PackageManager manager = act.getPackageManager();

            Intent intent = manager.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                Log.w(TAG,"game not found!(");
                return false;
            }
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(0);

        try {
            act.startActivityForResult(intent, IF_ACTIVITY_EXIST);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
