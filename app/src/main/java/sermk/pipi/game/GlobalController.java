package sermk.pipi.game;

import android.app.Application;
import android.content.res.Configuration;


/**
 * Created by echormonov on 30.10.17.
 */

public final class GlobalController extends Application {

    private UVCReciver mUVCReciver;

    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        LogOptions.SetupLog();
        // Required initialization logic here!
        mUVCReciver = new UVCReciver(this);
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

    public UVCReciver getUVCReciver(){
        return mUVCReciver;
    }
}
