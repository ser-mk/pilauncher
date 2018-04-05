package sermk.pipi.pilauncher.externalcooperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import sermk.pipi.pilauncher.UVCReciver;
import sermk.pipi.pilib.CommandCollection;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.MClient;
import sermk.pipi.pilib.UniversalReciver;

public class RequestReciever extends BroadcastReceiver {

    private String TAG = this.getClass().getName();

    private final ErrorCollector EC = new ErrorCollector();

    @Override
    public void onReceive(Context context, Intent intent) {
        EC.clear();

        final UniversalReciver.ReciverVarible rv
            = UniversalReciver.parseIntent(intent, TAG);

        String error = ErrorCollector.NO_ERROR;

        try {
            error = doAction(context, rv.action);
        } catch (Exception e){
            e.printStackTrace();
            error = e.toString();
        }

        if(ErrorCollector.NO_ERROR.equals(error)) return;

        EC.addError(error);

        Log.v(TAG, EC.error);

        MClient.sendMessage(context, EC.subjError(TAG, rv.action), EC.error);
    }

    private String doAction(Context context, final String action) {
        if (CommandCollection.ACTION_RECIVER_FOR_ALL_QUERY_SETTINGS.equals(action)) {
            AllSettings.getInstance().confirmSettings(context);
        } else if(CommandCollection.ACTION_RECIVER_CLEAR_CAPTURE_FRAME_INTERVAL.equals(action)){
            UVCReciver.clear_CAPTURE_FRAME_INTERVAL_PREV();
        } else {
            return "Undefined action!";
        }

        return ErrorCollector.NO_ERROR;
    }
}
