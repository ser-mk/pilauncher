package sermk.pipi.pilauncher.externalcooperation;

import android.app.Activity;

import android.content.Intent;
import android.util.Log;

/**
 * Created by echormonov on 10.12.17.
 */

final public class ClientWrapper {
    Activity act = null;
    private final String TAG = "ClientWrapper";
    public void setInstance(final Activity act){ this.act = act; }

    private static ClientWrapper instance = null;

    static public ClientWrapper getInstance(){
        if (instance == null){
            instance = new ClientWrapper();
        }
        return instance;
    }

    private Intent tempIntent(){
        Intent intent = new Intent();
        intent.setClassName(AllSettings.NAME_MC_PACKAGE(), AllSettings.NAME_MCS_SERVICE());
        return intent;
    }

    public boolean sendMessage(final String message){
        Intent intent = tempIntent();
        intent.putExtra(AllSettings.NAME_STRING_FIELD(),message);
        this.act.startService(intent);
        Log.v(TAG, "sending");
        return true;
    }
}
