package sermk.pipi.pilauncher;

import android.app.Application;
import android.content.res.Configuration;

import com.orhanobut.logger.Logger;

import org.opencv.android.OpenCVLoader;

import sermk.pipi.pilauncher.externalcooperation.AllSettings;
import sermk.pipi.pilauncher.externalcooperation.PiReceiver;


/**
 * Created by echormonov on 30.10.17.
 */

public final class GlobalController extends Application {

    private static GlobalController app = null;

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        LogOptions.SetupLog();
        AllSettings.getInstance().setInstance(this);
        // Required initialization logic here!
    }

    public static GlobalController getInstance(final String sObject){
        return app;
    }


    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        if(!(OpenCVLoader.initDebug())){
            Logger.w("Fail opencv load!");
        } else {
            Logger.v("succes opencv load library");
        }
    }

}
