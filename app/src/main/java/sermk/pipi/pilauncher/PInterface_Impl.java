package sermk.pipi.pilauncher;


import android.os.RemoteException;
import android.util.Log;

import sermk.pipi.pilib.PiBind;
import sermk.pipi.pilib.Pinterface;

/**
 * Created by echormonov on 24.11.17.
 */

public class PInterface_Impl extends Pinterface.Stub implements CVResolver.ICallbackPosition
{

    final String TAG = "PInterface_Impl";
    @Override
    public int getPosition() throws RemoteException {
        //Log.v(TAG,"getPosition " + position);
        return position;
    }

    private int position = PiBind.POSITION_UNDEFINED;

    @Override
    public boolean callbackPosition(int pos, CVResolver cvr) {
        //Log.v(TAG, "set position " + String.valueOf(pos));
        if(pos < PiBind.POSITION_MIN){
            position = PiBind.POSITION_UNDEFINED;
        } else {
            position = pos;
        }
        return true;
    }

    public void clearPosition(){
        Log.v(TAG, "clearPosition!");
        position = PiBind.POSITION_UNDEFINED;
    }

    public void sendingCloseCode(){
        Log.v(TAG, "CLOSE GAME!");
        position = PiBind.CLOSE_GAME;
    }
}
