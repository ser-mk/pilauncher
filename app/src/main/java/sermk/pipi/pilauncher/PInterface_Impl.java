package sermk.pipi.pilauncher;


import android.os.RemoteException;
import android.util.Log;

import sermk.pipi.pilib.Pinterface;

/**
 * Created by echormonov on 24.11.17.
 */

public class PInterface_Impl extends Pinterface.Stub implements CVResolver.ICallbackPosition
    {

    final String TAG = "PInterface_Impl";
    @Override
    public int getPosition() throws RemoteException {
        Log.v(TAG,"getPosition ");
        return 0;
    }

        @Override
        public boolean callbackPosition(int pos, CVResolver cvr) {
            return false;
        }
    }
