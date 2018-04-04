package sermk.pipi.pilauncher.externalcooperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import sermk.pipi.pilib.CommandCollection;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.MClient;
import sermk.pipi.pilib.UniversalReciver;

public class PiSettingsReciever extends BroadcastReceiver {

    private final String TAG = this.getClass().getName();

    private final ErrorCollector EC = new ErrorCollector();


    @Override
    public void onReceive(Context context, Intent intent) {

        EC.clear();

        final UniversalReciver.ReciverVarible rv
            = UniversalReciver.parseIntent(intent, TAG);

        String error = ErrorCollector.NO_ERROR;

        try {
            error = doAction(context, rv.content, rv.array, rv.action);
        } catch (Exception e){
            e.printStackTrace();
            error = e.toString();
        }

        if(ErrorCollector.NO_ERROR.equals(error)) return;

        EC.addError(error);

        Log.v(TAG, EC.error);

        MClient.sendMessage(context, EC.subjError(TAG, rv.action), EC.error);
    }

    private String doAction(Context context, final String content, final byte[] bytesArray, @NonNull final String action){
        if(action.equals(CommandCollection.ACTION_RECIVER_PILAUNCHER_SET_SETTINGS)){
            return setSettings(content, bytesArray);
        } else if (action.equals(CommandCollection.ACTION_RECIVER_PILAUNCHER_SAVE_SETTINGS)){
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
        Intent intent = new Intent(context, PiSettingsReciever.class);
        intent.setAction(action);

        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS,data);

        context.sendBroadcast(intent);
    }
}
