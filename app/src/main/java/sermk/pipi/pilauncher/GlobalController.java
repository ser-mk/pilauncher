package sermk.pipi.pilauncher;

import android.app.Application;
import android.content.res.Configuration;

import com.orhanobut.logger.Logger;

import org.opencv.android.OpenCVLoader;

import java.util.HashSet;

import sermk.pipi.pilauncher.externalcooperation.AllSettings;


/**
 * Created by echormonov on 30.10.17.
 */

public final class GlobalController extends Application {

    private static GlobalController app = null;

    private final String TAG = this.getClass().getName();

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        LogOptions.SetupLog();
        AllSettings.getInstance().init(this);
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

    public static class Problem {
        private HashSet<String> SetProblem = new HashSet<String>();

        public HashSet<String> problemSet() {
            return SetProblem;
        }

        public String getAllStatus(){
            String ret = new String();
            for(String s: SetProblem){
                ret += s;
            }
            return ret;
        }
    }

    public Problem problem = new Problem();

}
