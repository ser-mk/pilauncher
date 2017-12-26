package sermk.pipi.pilauncher.externalcooperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PingReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.v(TAG,"! " + context.getClass().getName()
            + context.getPackageCodePath()
            + context.getPackageName() + "\n"
            + context.getApplicationInfo()
            + context.getApplicationContext().getClass().getName()
            + "\n" + intent.toString());

        String action = "";
        try{
            action = intent.getStringExtra(Intent.ACTION_MAIN);
            action.isEmpty();
        } catch (Exception e){
            action = "wrong action!";
            Log.w(TAG, "action is not exist!");
        }
        Log.v(TAG, action);

        String content = "";
        try{
            content = intent.getStringExtra(Intent.EXTRA_TEXT);
            content.isEmpty();
        } catch (Exception e){
            content = "wrong content!";
            Log.w(TAG, "content is not exist!");
        }
        Log.v(TAG, content);

        byte[] array = new byte[0];
        try{
            array = intent.getByteArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
            array.hashCode();
        } catch (Exception e){
            array = "wrong byte array !".getBytes();
            Log.w(TAG, "attached data absent!");
        }
    }
}
