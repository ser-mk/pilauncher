package sermk.pipi.pilauncher.externalcooperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import sermk.pipi.pilauncher.R;

public class PiReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getName();
    static String ACTION_RECIVER_SAVE_SETTINGS = "";
    static String ACTION_RECIVER_SET_SETTINGS = "";

    public static void init(Context context){
        ACTION_RECIVER_SAVE_SETTINGS = context.getResources().getString(R.string.ACTION_RECIVER_SAVE_SETTINGS);
        ACTION_RECIVER_SET_SETTINGS = context.getResources().getString(R.string.ACTION_RECIVER_SET_SETTINGS);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.v(TAG, "inent: " + intent.toString());
        String action;
        try{
            action = intent.getAction();
            action.isEmpty();
        } catch (Exception e){
            action = "wrong action!";
            Log.w(TAG, "action is not exist!");
        }
        Log.v(TAG, action);

        String content;
        try{
            content = intent.getStringExtra(Intent.EXTRA_TEXT);
            content.isEmpty();
        } catch (Exception e){
            content = "wrong content!";
            Log.w(TAG, "content is not exist!");
        }
        Log.v(TAG, content);

        byte[] bytesArray;
        try{
            bytesArray = intent.getByteArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
            bytesArray.hashCode();
        } catch (Exception e){
            bytesArray = "wrong byte array !".getBytes();
            Log.w(TAG, "attached data absent!");
        }

        doAction(context, content, bytesArray, action);
    }

    private boolean doAction(Context context, final String content, final byte[] bytesArray, @NonNull final String action){
        if(action.equals(ACTION_RECIVER_SET_SETTINGS)){
            return setSettings(content, bytesArray);
        } else if (action.equals(ACTION_RECIVER_SAVE_SETTINGS)){
            return saveSettings(context);
        }

        Log.w(TAG, "undefined action!");

        return false;
    }

    private boolean setSettings(final String content, final byte[] bytesArray){
        AllSettings.getInstance().setCurrentSettings(content, bytesArray);
        return true;
    }

    private boolean saveSettings(Context context){
        AllSettings.getInstance().saveCurrentSettings(context);
        return true;
    }

    static void sendBroadCastData(Context context, final String action,
                                  final String content, final byte[] data){
        Intent intent = new Intent(context, PiReceiver.class);
        intent.setAction(action);

        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS,data);

        context.sendBroadcast(intent);
    }
}
