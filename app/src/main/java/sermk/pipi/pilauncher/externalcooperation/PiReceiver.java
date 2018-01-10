package sermk.pipi.pilauncher.externalcooperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import sermk.pipi.pilauncher.R;
import sermk.pipi.pilib.CommandCollection;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.MClient;

public class PiReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getName();

    private final ErrorCollector EC = new ErrorCollector();


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        EC.clear();
        Log.v(TAG, "inent: " + intent.toString());
        String action;
        try{
            action = intent.getAction().trim();
        } catch (Exception e){
            action = "wrong action!";
            EC.addError(action);
            Log.w(TAG, "action is not exist!");
        }
        Log.v(TAG, action);

        String content;
        try{
            content = intent.getStringExtra(Intent.EXTRA_TEXT).trim();
        } catch (Exception e){
            content = "wrong content!";
            EC.addError(content);
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

        String success = ErrorCollector.NO_ERROR;

        try {
            success = doAction(context, content, bytesArray, action);
        } catch (Exception e){
            e.printStackTrace();
            EC.addError(EC.getStackTraceString(e));
            success = e.toString();
        }

        if(ErrorCollector.NO_ERROR.equals(success)) return;

        Log.v(TAG, EC.error);

        MClient.sendMessage(context, EC.subjError(TAG,action), EC.error);
    }

    private String doAction(Context context, final String content, final byte[] bytesArray, @NonNull final String action){
        if(action.equals(CommandCollection.ACTION_RECIVER_SET_SETTINGS)){
            return setSettings(content, bytesArray);
        } else if (action.equals(CommandCollection.ACTION_RECIVER_SAVE_SETTINGS)){
            return saveSettings(context);
        }

        final String err = "undefined action!";
        Log.w(TAG, err);
        EC.addError(err);

        return err;
    }

    private String setSettings(final String content, final byte[] bytesArray){
        return AllSettings.getInstance().setCurrentSettings(content, bytesArray);
    }

    private String saveSettings(Context context){
        return AllSettings.getInstance().saveCurrentSettings(context);
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
