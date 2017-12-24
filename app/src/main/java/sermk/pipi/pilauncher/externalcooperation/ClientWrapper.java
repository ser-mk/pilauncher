package sermk.pipi.pilauncher.externalcooperation;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by echormonov on 10.12.17.
 */

final public class ClientWrapper {
    Activity act = null;
    private final String TAG = "ClientWrapper";
    public void setContext(final Activity act){ this.act = act; }

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

    public boolean sendMessage(@NonNull final String subject, @NonNull final String data,
                                                      final byte[] attached_data){
        Intent intent = tempIntent();
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, data);
        //intent.putExtra(Intent.EXTRA_STREAM, new String[0]);

        final ComponentName c = this.act.startService(intent);
        if(c == null){
            Log.w(TAG, "sent FAILED!");
            return false;
        } else {
            Log.v(TAG, "sent succes");
        }
        return true;
    }
}
